import { ref, reactive, onMounted, computed, nextTick, watch } from 'vue';
import { shopApi, goodsApi, shoppingCartApi, reviewApi } from '@/api/index';
import GoodsItem from '@/components/GoodsItem.vue';
import ShopCart from '@/components/ShopCart.vue';
import SetmealPanel from '@/components/SetmealPanel.vue';
import DishPanel from '@/components/DishPanel.vue';

export default {
  components: {
    GoodsItem,
    ShopCart,
    SetmealPanel,
    DishPanel
  },
  setup() {
    
    // 获取路由参数
    const shopId = ref('');

    onMounted(() => {
      const pages = getCurrentPages();
      if (!pages.length) return; // 防止 pages 为空

      const currentPage = pages[pages.length - 1];

      // 兼容微信小程序和 uni-app
      const options = currentPage.options || currentPage.$page?.options;

      console.log(options);
      if (options && options.id) {
        shopId.value = options.id;

        // 存储当前店铺ID到本地，方便其他页面使用
        uni.setStorageSync('shopId', shopId.value);

        getShopDetail();   // 获取店铺基本信息
        getCategoryList(); // 获取分类列表
        getShopCartList(); // 获取该店铺的购物车数据
        loadReviews();     // 获取评论列表
      }
    });

    // 店铺信息
    const shopInfo = ref({});
    const shopNotice = ref('失败了，就失败了，能把采条吃下去就是胜利');
    const shopStatus = ref(1); // 店铺营业状态：1=营业中，0=打烊中

    // 格式化后的店铺信息（用于页面显示）
    // computed：Vue3计算属性，依赖数据变化自动更新，缓存结果
    const formattedShopInfo = computed(() => {
       // 如果店铺信息还没获取到，返回空对象，避免页面报错
	  if (!shopInfo.value) return {};

      return {
		 // 展开后端返回的原始店铺所有数据（名称、图片、评分等）  
        ...shopInfo.value,
		// 格式化起送价：后端单位是【分】，前端要转成【元】，保留2位小数
        formattedMinFee: shopInfo.value.minFee ? (shopInfo.value.minFee / 100).toFixed(2) : '0.00',
         // 格式化配送费：同理，分转元，保留2位小数
		formattedDeliverFee: shopInfo.value.deliverFee ? (shopInfo.value.deliverFee / 100).toFixed(2) : '0.00',
        // 保存原始金额（单位：分），用于后续购物车计算
		rawMinFee: shopInfo.value.minFee || 0,
		rawDeliverFee: shopInfo.value.deliverFee || 0
      };
    });

    // 获取店铺详情
    const getShopDetail = async () => {
      console.log("发送获取detail请求");
      try {
        const res = await shopApi.getShopDetail(shopId.value);
        if (res.code === 1 && res.data) {
          shopInfo.value = res.data;
          console.log('获取到店铺信息:', shopInfo.value);
          console.log('起送费(原始值):', shopInfo.value.minFee, '(显示值:', shopInfo.value.minFee / 100, ')');
          console.log('配送费(原始值):', shopInfo.value.deliverFee, '(显示值:', shopInfo.value.deliverFee / 100, ')');
          // 确保金额显示正确
          if (typeof shopInfo.value.minFee === 'number' && typeof shopInfo.value.deliverFee === 'number') {
            console.log('起送费和配送费数据类型正确');
          } else {
            console.warn('起送费或配送费数据类型不正确，minFee:', typeof shopInfo.value.minFee, 'deliverFee:', typeof shopInfo.value.deliverFee);
          }
        }

        // 获取店铺营业状态
        try {
          const statusRes = await shopApi.getShopStatus(shopId.value);
          if (statusRes.code === 1 && statusRes.data !== null && statusRes.data !== undefined) {
            shopStatus.value = statusRes.data;
            console.log('获取到店铺营业状态:', shopStatus.value === 1 ? '营业中' : '打烊中');
          }
        } catch (statusErr) {
          console.error('获取店铺营业状态失败:', statusErr);
          // 默认营业中，不影响正常使用
        }
      } catch (error) {
        console.error('获取店铺详情失败:', error);
        uni.showToast({
          title: '获取店铺详情失败',
          icon: 'none'
        });
      }
    };

    // 当前激活的选项卡menu(点餐)、comment(评价)、merchant(商家)
    const activeTab = ref('menu');
    const switchTab = (tab) => {
      activeTab.value = tab;

      // 如果切换到商家页面，确保店铺信息已加载
      if (tab === 'merchant' && (!shopInfo.value || !shopInfo.value.id)) {
        getShopDetail();
      }

      // 如果切换到商家页面，打印调试信息
      if (tab === 'merchant') {
        console.log('切换到商家页面');
        console.log('起送费(原始值):', shopInfo.value.minFee, '(显示值:', shopInfo.value.minFee / 100, ')');
        console.log('配送费(原始值):', shopInfo.value.deliverFee, '(显示值:', shopInfo.value.deliverFee / 100, ')');
      }
    };

    // 返回上一页
    const goBack = () => {
      uni.navigateBack();
    };

    // 显示公告
    const showNotice = () => {
      uni.showModal({
        title: '店铺公告',
        content: shopNotice.value,
        showCancel: false
      });
    };

    // 商品分类
    const categories = ref([]);  // 分类列表
    const currentCategory = ref(0);  // 当前选中的分类索引
    const loadingCategories = ref(new Set()); // 记录正在加载中的分类

    // 获取分类列表
    const getCategoryList = async () => {
      try {
        const res = await goodsApi.getCategoryList(shopId.value);

        if (res.code === 1 && res.data) {
          // 对分类列表进行排序，确保type为1的普通分类在前，type为2的套餐分类在后
          const sortedCategories = [...res.data].sort((a, b) => {
            // 如果两个分类类型不一样：普通(1)在前，套餐(2)在后
            if (a.type !== b.type) {
              return a.type - b.type;
            }
            // 如果类型一样：按后端给的 sort 数字从小到大排序
            return a.sort - b.sort;
          });

		 // 加工数据】给每个分类，新增页面要用的字段
          categories.value = sortedCategories.map(item => {
            return {
              ...item,  // 保留后端原本给的所有信息（分类id、名称、图片等）
              count: 0, // 初始化购物车数量
              dishList: [], // 存储该分类下的菜品
              loaded: false, // 标记该分类是否已加载数据
              isEmpty: false, // 标记该分类是否为空
              loadError: false // 标记加载是否出错
            };
          });

          if (categories.value.length > 0) {
            // 自动加载第一个分类的菜品（打开页面直接显示第一个分类的菜）
            intelligentLoadCategories();
          }
        }
      } catch (error) {
        console.error('获取分类列表失败:', error);
        uni.showToast({
          title: '获取分类列表失败',
          icon: 'none'
        });
      }
    };

    // 智能加载分类数据
	// 核心作用：打开店铺页面时，不一次性加载所有分类的菜品（太卡）
	// 只先加载【可视区域内 + 附近】的分类，提升页面打开速度
    const intelligentLoadCategories = () => {
       // 如果一个分类都没有，直接结束，不执行后续逻辑
      if (categories.value.length === 0) return;

      // 立即加载【第一个分类】的菜品
      // 保证用户一进页面就能看到菜
      loadCategoryDishes(0);

      // 如果总共就1个分类，加载完第一个就结束，不用往下走
      if (categories.value.length === 1) return;

      //最多预加载前3个分类（避免一次性请求太多，服务器压力大/页面卡顿）
      // Math.min(3, 总长度) → 最多3个，不够3个就加载全部
      const maxInitLoad = Math.min(3, categories.value.length);

       // 从第2个分类开始，延迟加载后面的分类
       // 错开时间发请求，避免同时请求太多导致卡顿
      for (let i = 1; i < maxInitLoad; i++) {
         // 延迟时间：第2个延迟300ms，第3个延迟600ms → 错开请求
		const delay = i * 300; 
        // 延迟执行
		setTimeout(() => {
			// 判断：如果这个分类还没加载过，才去加载
          if (!categories.value[i].loaded) {
            loadCategoryDishes(i);
          }
        }, delay);
      }
    };

    // 加载分类下的菜品
    const loadCategoryDishes = async (index) => {
      const category = categories.value[index];
      if (!category || category.loaded || loadingCategories.value.has(category.id)) {
        return;
      }

      loadingCategories.value.add(category.id);

      // 添加超时处理，防止长时间加载
      const timeoutId = setTimeout(() => {
        if (loadingCategories.value.has(category.id)) {
          // 如果超时，设置为已加载状态，但dishList为空数组
          loadingCategories.value.delete(category.id);
          categories.value[index].loaded = true;
          console.warn(`分类 ${category.name} 数据加载超时`);
        }
      }, 5000); // 5秒超时

      try {
        // 根据分类类型决定调用哪个接口
        const isSetmeal = category.type === 2; // type=2 是套餐分类

        // 明确指定API
        let api;
        if (isSetmeal) {
          api = goodsApi.getSetmealList;
        } else {
          api = goodsApi.getDishList;
        }

        const res = await api(category.id);

        // 清除超时定时器
        clearTimeout(timeoutId);

        if (res.code === 1) {
          // 为菜品/套餐数据添加销量字段
          const dishes = res.data ? res.data.map(dish => ({
            ...dish,
            quantitySold: dish.quantitySold || 0, // 确保quantitySold存在且为数字
            isSetmeal: isSetmeal // 添加标记，区分套餐和普通菜品
          })) : [];

          categories.value[index].dishList = dishes;
          categories.value[index].loaded = true;

          // 如果返回的菜品数据为空，直接设置为空状态，避免显示加载中
          if (!dishes.length) {
            categories.value[index].isEmpty = true;
          }
        } else {
          // 接口返回失败也标记为已加载，避免一直显示加载状态
          categories.value[index].loaded = true;
          categories.value[index].isEmpty = true;
          console.error(`获取${category.type === 2 ? '套餐' : '菜品'}列表失败: `, res.msg);
        }
      } catch (error) {
        // 清除超时定时器
        clearTimeout(timeoutId);

        console.error(`获取${category.type === 2 ? '套餐' : '菜品'}列表失败:`, error);
        // 加载失败也标记为已加载，避免一直显示加载状态
        categories.value[index].loaded = true;
        categories.value[index].isEmpty = true;
      } finally {
        loadingCategories.value.delete(category.id);
      }
    };

    // 预加载邻近分类的菜品
    const preloadNearbyCategories = (currentIndex) => {
      if (!categories.value.length) return;

      // 预加载当前分类的前两个和后两个分类的菜品，增加预加载范围
      const preloadRadius = 2; // 预加载当前分类前后2个分类

      for (let offset = -preloadRadius; offset <= preloadRadius; offset++) {
        if (offset === 0) continue; // 跳过当前分类

        const targetIndex = currentIndex + offset;
        // 确保索引有效
        if (targetIndex >= 0 && targetIndex < categories.value.length) {
          const category = categories.value[targetIndex];
          // 避免重复加载或正在加载的分类
          if (!category.loaded && !loadingCategories.value.has(category.id)) {
            // 计算延迟时间，优先加载更靠近当前分类的分类
            const delay = Math.abs(offset) * 200;
            setTimeout(() => {
              loadCategoryDishes(targetIndex);
            }, delay);
          }
        }
      }
    };

    // 选择分类
    const selectCategory = (index) => {
      // 更新当前分类
      currentCategory.value = index;

      // 加载该分类下的菜品(如果还没加载)
      loadCategoryDishes(index);

      // 预加载邻近分类
      preloadNearbyCategories(index);

      // 滚动到对应位置
      scrollToViewId.value = `category-${index}`;
    };

    // 用于存储滚动到视图的ID
    const scrollToViewId = ref('');

    // 商品滚动事件
    const onGoodsScroll = (e) => {
      // 防抖处理，避免频繁触发
      if (scrollTimer.value) {
        clearTimeout(scrollTimer.value);
      }

      scrollTimer.value = setTimeout(() => {
        const scrollTop = e.detail.scrollTop;
        updateCurrentCategoryByScrollPosition(scrollTop);
        scrollTimer.value = null;
      }, 100); // 减少节流时间，提高响应速度
    };

    // 根据滚动位置更新当前分类
    const updateCurrentCategoryByScrollPosition = (scrollTop) => {
      // 首先尝试按照预估高度计算当前分类（作为备用方案）
      const estimatedHeight = 300; // 估计每个分类的平均高度
      let estimatedIndex = Math.floor(scrollTop / estimatedHeight);

      // 确保索引在有效范围内
      if (estimatedIndex < 0) {
        estimatedIndex = 0;
      } else if (estimatedIndex >= categories.value.length) {
        estimatedIndex = categories.value.length - 1;
      }

      // 如果当前分类未变，或者滚动位置很小（接近顶部），直接使用第一个分类
      if (scrollTop < 50) {
        if (currentCategory.value !== 0) {
          currentCategory.value = 0;
          // 确保第一个分类的数据已加载
          if (!categories.value[0].loaded) {
            loadCategoryDishes(0);
          }
          // 预加载下一个分类
          preloadNearbyCategories(0);
        }
        return;
      }

      // 如果当前估算的分类与当前选中的分类不同，且尚未加载，则加载该分类数据
      if (estimatedIndex !== currentCategory.value) {
        currentCategory.value = estimatedIndex;

        // 加载估算分类的数据（如果尚未加载）
        if (!categories.value[estimatedIndex].loaded) {
          loadCategoryDishes(estimatedIndex);
        }

        // 预加载相邻分类的数据
        preloadNearbyCategories(estimatedIndex);
      }

      // 即使当前分类未变，也预加载周围分类数据，提高用户体验
      const preloadRadius = 2; // 预加载当前分类前后各2个分类
      for (let i = Math.max(0, estimatedIndex - preloadRadius);
        i <= Math.min(categories.value.length - 1, estimatedIndex + preloadRadius);
        i++) {
        if (i !== estimatedIndex && !categories.value[i].loaded && !loadingCategories.value.has(categories.value[i].id)) {
          setTimeout(() => {
            loadCategoryDishes(i);
          }, (i - estimatedIndex) * 200); // 错开请求时间
        }
      }
    };

    // 滚动防抖计时器
    const scrollTimer = ref(null);

    // 购物车
    const cartList = ref([]);

    // 套餐面板状态
    const showSetmealPanel = ref(false);
    const currentSetmeal = ref({});

    // 菜品面板状态
    const showDishPanel = ref(false);
    const currentDish = ref({});

    // 获取该店铺的购物车列表
    const getShopCartList = async () => {
      try {
        const res = await shoppingCartApi.listShoppingCartByShopId(shopId.value);
        if (res.code === 1 && res.data) {
          // 更新购物车数据
          const cartItems = res.data.map(item => {
            // 构建完整的购物车项目对象
            const cartItem = {
              id: item.dishId || item.setmealId,
              count: item.number || 1,
              price: item.amount,
              name: item.name,
              image: item.image,
              isSetmeal: !!item.setmealId,
              type: item.setmealId ? 'setmeal' : 'dish'
            };

            // 如果有口味数据，解析并添加
            if (item.dishFlavor) {
              try {
                const flavorObj = JSON.parse(item.dishFlavor);
                cartItem.selectedFlavors = flavorObj;

                // 将口味对象转为文本描述
                const flavorText = Object.entries(flavorObj)
                  .map(([name, value]) => `${name}: ${value}`)
                  .join(', ');

                cartItem.flavorText = flavorText;
              } catch (e) {
                console.error('解析口味数据出错', e);
              }
            }

            return cartItem;
          });

          cartList.value = cartItems;

          // 更新分类中的商品数量
          updateCategoryCount();
        }
      } catch (error) {
        console.error('获取购物车数据失败:', error);
      }
    };

    // 添加商品到购物车
    const addToCart = async (goods) => {
      try {
        // 确保cartList.value是数组
        if (!Array.isArray(cartList.value)) {
          console.error('购物车数据不是数组, 正在重置:', cartList.value);
          cartList.value = [];
        }

        // 确保goods是有效的商品对象
        if (!goods || typeof goods !== 'object' || !goods.id) {
          console.error('无效的商品数据:', goods);
          return;
        }

        // 如果是减少商品数量
        if (goods.isReduce) {
          // 减少商品
          const index = cartList.value.findIndex(item => item.id === goods.id);
          if (index > -1) {
            if (cartList.value[index].count > 1) {
              cartList.value[index].count -= 1;
            } else {
              cartList.value.splice(index, 1);
            }
            // 更新分类中的商品数量
            updateCategoryCount();
          }
          return;
        }

        // 构建API请求参数
        const shoppingCartData = {
          shopId: shopInfo.value.id,
          dishId: null,
          setmealId: null,
        };

        // 根据商品类型设置dishId或setmealId
        if (goods.type === 'setmeal' || goods.isSetmeal) {
          shoppingCartData.setmealId = goods.id;
        } else {
          shoppingCartData.dishId = goods.id;
        }

        // 添加口味数据（如果有）
        if (goods.selectedFlavors && Object.keys(goods.selectedFlavors).length > 0) {
          shoppingCartData.dishFlavor = JSON.stringify(goods.selectedFlavors);
        }

        console.log('正在添加商品到购物车:', shoppingCartData);

        // 如果商品是从面板添加的(fromPanel)或直接添加的(isDirectAdd)，跳过API调用
        // 因为这些请求已经在组件内部发送过了
        if (goods.fromPanel || goods.isDirectAdd) {
          console.log('商品已经通过组件发送过请求，跳过重复API调用');

          // 只处理本地购物车更新逻辑
          const index = cartList.value.findIndex(item => item.id === goods.id);
          if (index > -1) {
            cartList.value[index].count += 1;
          } else {
            // 处理套餐内菜品数据，确保套餐数据完整
            let cartItem = { ...goods, count: 1 };

            // 如果是套餐，确保有正确的标识和菜品数据
            if (goods.isSetmeal || goods.type === 'setmeal') {
              cartItem.isSetmeal = true;
              cartItem.type = 'setmeal';

              // 将菜品数据标准化到setmealDishes中
              // 优先使用setmealDishes
              if (goods.setmealDishes && Array.isArray(goods.setmealDishes) && goods.setmealDishes.length > 0) {
                // 深拷贝防止引用问题
                cartItem.setmealDishes = JSON.parse(JSON.stringify(goods.setmealDishes));
              }
              // 其次使用dishList
              else if (goods.dishList && Array.isArray(goods.dishList) && goods.dishList.length > 0) {
                // 深拷贝并标准化数据结构
                cartItem.setmealDishes = JSON.parse(JSON.stringify(goods.dishList.map(dish => ({
                  ...dish,
                  id: dish.id || null,
                  setmealId: goods.id,
                  dishId: dish.dishId || dish.id || null,
                  name: dish.name,
                  price: dish.price || 0,
                  copies: dish.copies || 1
                }))));
                console.log('已将套餐菜品数据复制到setmealDishes', cartItem.name, cartItem.setmealDishes.length);
              }
            }

            cartList.value.push(cartItem);
          }

          // 更新分类中的商品数量
          updateCategoryCount();
          return;
        }

        // 调用添加购物车API
        const result = await shoppingCartApi.addShoppingCart(shoppingCartData);

        if (result.code === 1) {
          // 增加商品到本地购物车进行UI展示
          const index = cartList.value.findIndex(item => item.id === goods.id);
          if (index > -1) {
            cartList.value[index].count += 1;
          } else {
            // 处理套餐内菜品数据，确保套餐数据完整
            let cartItem = { ...goods, count: 1 };

            // 如果是套餐，确保有正确的标识和菜品数据
            if (goods.isSetmeal || goods.type === 'setmeal') {
              cartItem.isSetmeal = true;
              cartItem.type = 'setmeal';

              // 将菜品数据标准化到setmealDishes中
              // 优先使用setmealDishes
              if (goods.setmealDishes && Array.isArray(goods.setmealDishes) && goods.setmealDishes.length > 0) {
                // 深拷贝防止引用问题
                cartItem.setmealDishes = JSON.parse(JSON.stringify(goods.setmealDishes));
              }
              // 其次使用dishList
              else if (goods.dishList && Array.isArray(goods.dishList) && goods.dishList.length > 0) {
                // 深拷贝并标准化数据结构
                cartItem.setmealDishes = JSON.parse(JSON.stringify(goods.dishList.map(dish => ({
                  ...dish,
                  id: dish.id || null,
                  setmealId: goods.id,
                  dishId: dish.dishId || dish.id || null,
                  name: dish.name,
                  price: dish.price || 0,
                  copies: dish.copies || 1
                }))));
                console.log('已将套餐菜品数据复制到setmealDishes', cartItem.name, cartItem.setmealDishes.length);
              }
            }

            cartList.value.push(cartItem);
          }

          // 更新分类中的商品数量
          updateCategoryCount();

          // 成功提示
          uni.showToast({
            title: '已加入购物车',
            icon: 'success'
          });
        } else {
          uni.showToast({
            title: result.msg || '添加购物车失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('添加商品到购物车出错:', error, goods);
        uni.showToast({
          title: '操作失败，请重试',
          icon: 'none'
        });
      }
    };

    // 更新分类中的商品数量
    const updateCategoryCount = () => {
      // 确保cartList.value是数组
      if (!Array.isArray(cartList.value)) {
        console.error('购物车数据不是数组:', cartList.value);
        cartList.value = []; // 重置为空数组
        return;
      }

      // 重置所有分类的计数
      categories.value.forEach(category => {
        category.count = 0;
      });

      // 根据购物车中的商品计算各分类的数量
      cartList.value.forEach(item => {
        if (!item || typeof item !== 'object' || !item.id) {
          console.warn('购物车中存在无效商品:', item);
          return; // 跳过无效商品
        }

        // 遍历所有分类
        categories.value.forEach(category => {
          if (category.dishList && Array.isArray(category.dishList)) {
            // 检查该分类下是否有这个商品
            const hasItem = category.dishList.some(dish => dish && dish.id === item.id);
            if (hasItem) {
              category.count += item.count;
            }
          }
        });
      });
    };

    // 更新购物车
    const updateCart = (cart) => {
      // 确保cartList.value始终是数组
      if (Array.isArray(cart)) {
        cartList.value = cart;
      } else if (cart) {
        // 如果传入了非数组的cart，需要处理成数组
        console.warn('传入的cart不是数组类型，正在尝试修复...', cart);
        // 如果传入的是一个商品对象，将其转为只包含该商品的数组
        if (typeof cart === 'object' && cart !== null && cart.id) {
          // 查找当前购物车中是否已存在该商品
          const existingIndex = cartList.value.findIndex(item => item.id === cart.id);
          // 创建购物车数组的副本
          const newCartList = [...cartList.value];

          if (cart.count === 0) {
            // 如果数量为0，从购物车中移除
            if (existingIndex > -1) {
              newCartList.splice(existingIndex, 1);
            }
          } else if (existingIndex > -1) {
            // 如果已存在，更新数量
            newCartList[existingIndex] = { ...cart };
          } else {
            // 如果不存在，添加到购物车
            newCartList.push({ ...cart });
          }

          cartList.value = newCartList;
        } else {
          // 如果传入的数据无法处理，保持原有购物车不变
          console.error('无法处理传入的购物车数据:', cart);
        }
      } else {
        // 如果传入undefined或null，重置为空数组
        cartList.value = [];
      }

      // 更新分类中的商品数量
      updateCategoryCount();
    };

    // 评论相关数据
    const reviewList = ref([]);
    const reviewTotal = ref(0);
    const reviewLoading = ref(false);
    const reviewPage = ref(1);
    const reviewPageSize = ref(10);
    const reviewRating = ref(null); // 评分筛选：null-全部，5-好评，4-中评，3-差评

    // 评论筛选选项
    const reviewFilters = [
      { label: '全部', value: null },
      { label: '好评', value: 5 },
      { label: '中评', value: 4 },
      { label: '差评', value: 3 }
    ];

    // 获取评论列表
    const loadReviews = async () => {
      if (reviewLoading.value) return;

      try {
        reviewLoading.value = true;

        const params = {
          shopId: shopId.value,
          page: reviewPage.value,
          pageSize: reviewPageSize.value,
          rating: reviewRating.value || undefined
        };

        const res = await reviewApi.getReviewList(params);

        if (res.code === 1 && res.data) {
          const { records, total } = res.data;

          // 格式化评论数据
          const formattedReviews = records.map(review => ({
            ...review,
            // 格式化时间
            createTime: formatDate(review.createTime),
            // 确保photoUrls是数组
            photoUrls: Array.isArray(review.photoUrls) ? review.photoUrls : [],
            // 确保replies是数组
            replies: Array.isArray(review.replies) ? review.replies : []
          }));

          // 追加或替换评论列表
          if (reviewPage.value === 1) {
            reviewList.value = formattedReviews;
          } else {
            reviewList.value = [...reviewList.value, ...formattedReviews];
          }

          reviewTotal.value = total;
        }
      } catch (error) {
        console.error('获取评论列表失败:', error);
        uni.showToast({
          title: '获取评论失败',
          icon: 'none'
        });
      } finally {
        reviewLoading.value = false;
      }
    };

    // 切换评论筛选
    const switchReviewFilter = (rating) => {
      if (reviewRating.value === rating) return;
      reviewRating.value = rating;
      reviewPage.value = 1;
      reviewList.value = [];
      loadReviews();
    };

    // 加载更多评论
    const loadMoreReviews = () => {
      if (reviewList.value.length >= reviewTotal.value) return;
      reviewPage.value++;
      loadReviews();
    };

    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    };

    // 监听选项卡切换
    watch(activeTab, (newTab) => {
      if (newTab === 'comment' && reviewList.value.length === 0) {
        loadReviews();
      }
    });

    // 预览图片
    const previewImage = (urls, current) => {
      uni.previewImage({
        urls: urls,
        current: current
      });
    };

    // 显示套餐详情面板
    const showSetmealDetail = async (setmeal) => {
      try {
        // 显示加载中状态
        uni.showLoading({
          title: '加载中...',
          mask: true
        });

        // 调用API获取完整的套餐详情
        const res = await goodsApi.getSetmealDetail(setmeal.id);

        // 隐藏加载提示
        uni.hideLoading();

        if (res.code === 1 && res.data) {
          // 添加标记，确保在前端处理时知道这是套餐
          const detailedSetmeal = {
            ...res.data,
            isSetmeal: true,
            sales: res.data.sales || setmeal.sales || 0
          };

          // 更新当前套餐并显示面板
          currentSetmeal.value = detailedSetmeal;
          showSetmealPanel.value = true;
        } else {
          // 如果获取详情失败，仍然使用原来的数据显示
          console.warn('获取套餐详情失败，使用列表中的简略信息:', res.msg);
          currentSetmeal.value = setmeal;
          showSetmealPanel.value = true;

          // 提示用户
          uni.showToast({
            title: '获取套餐详情失败',
            icon: 'none'
          });
        }
      } catch (error) {
        // 错误处理
        uni.hideLoading();
        console.error('获取套餐详情出错:', error);

        // 出错时仍使用原数据显示
        currentSetmeal.value = setmeal;
        showSetmealPanel.value = true;

        // 提示用户
        uni.showToast({
          title: '获取套餐详情失败',
          icon: 'none'
        });
      }
    };

    // 关闭套餐详情面板
    const closeSetmealPanel = () => {
      showSetmealPanel.value = false;
    };

    // 确认添加套餐到购物车
    const confirmAddSetmeal = (setmeal) => {
      // 确保套餐数据标记正确
      const setmealToAdd = {
        ...setmeal,
        isSetmeal: true,
        type: 'setmeal'
      };

      // 添加到购物车
      addToCart(setmealToAdd);
      closeSetmealPanel();
    };

    // 显示菜品详情面板
    const showDishDetail = (dish) => {
      currentDish.value = dish;
      showDishPanel.value = true;
    };

    // 关闭菜品详情面板
    const closeDishPanel = () => {
      showDishPanel.value = false;
    };

    // 确认添加菜品到购物车
    const confirmAddDish = (dish) => {
      addToCart(dish);
      closeDishPanel();
    };

    // 查看商品详情
    const viewGoodsDetail = (goods) => {
      // 判断是套餐还是普通菜品，显示对应的面板
      if (goods.isSetmeal || goods.type === 'setmeal') {
        // 调用showSetmealDetail获取并显示套餐详情
        showSetmealDetail(goods);
      } else {
        // 显示普通菜品详情
        showDishDetail(goods);
      }
    };

    // 解析口味选项
    const parseFlavorOptions = (flavors) => {
      if (!flavors || !flavors.length) return '';

      return flavors.map(flavor => {
        try {
          const valueStr = flavor.value || '[]';
          const values = JSON.parse(valueStr);
          return `${flavor.name}: ${values.join(', ')}`;
        } catch (e) {
          return `${flavor.name}: ${flavor.value}`;
        }
      }).join('\n');
    };

    return {
      shopId,
      shopInfo,
      shopNotice,
      shopStatus,
      activeTab,
      switchTab,
      goBack,
      showNotice,
      categories,
      currentCategory,
      selectCategory,
      onGoodsScroll,
      scrollToViewId,
      cartList,
      addToCart,
      updateCart,
      getShopCartList,
      reviewList,
      reviewTotal,
      reviewLoading,
      reviewFilters,
      reviewRating,
      switchReviewFilter,
      loadMoreReviews,
      previewImage,
      viewGoodsDetail,
      showSetmealDetail,
      showSetmealPanel,
      currentSetmeal,
      closeSetmealPanel,
      confirmAddSetmeal,
      showDishPanel,
      currentDish,
      showDishDetail,
      closeDishPanel,
      confirmAddDish,
      parseFlavorOptions,
      formattedShopInfo
    };
  }
};