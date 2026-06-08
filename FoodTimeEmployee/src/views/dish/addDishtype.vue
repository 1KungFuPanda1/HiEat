<template>
  <div :key="vueRest" class="addBrand-container">
    <div :key="restKey" class="container">
      <el-form ref="ruleForm" :model="ruleForm" :rules="rules" :inline="true" label-width="180px" class="demo-ruleForm">
        <div>
          <el-form-item label="菜品名称:" prop="name">
            <el-input v-model="ruleForm.name" placeholder="请填写菜品名称" maxlength="20" />
          </el-form-item>
          <el-form-item label="菜品分类:" prop="categoryId">
            <el-select v-model="ruleForm.categoryId" placeholder="请选择菜品分类">
              <el-option v-for="(item, index) in dishList" :key="index" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </div>
        <div>
          <el-form-item label="菜品价格:" prop="price">
            <el-input v-model="ruleForm.price" placeholder="请设置菜品价格" />
          </el-form-item>
        </div>
        <el-form-item label="口味做法配置:">
          <el-form-item>
            <div class="flavorBox">
              <span v-if="dishFlavors.length == 0" class="addBut" @click="addFlavore">
                + 添加口味</span>
              <div v-if="dishFlavors.length != 0" class="flavor">
                <div class="title">
                  <span>口味名（3个字内）</span>
                </div>
                <div class="cont">
                  <div v-for="(item, index) in dishFlavors" :key="index" class="items">
                    <div class="itTit">
                      <SelectInput :dish-flavors-data="leftDishFlavors" :index="index" :value="item.name"
                        @select="selectHandle" />
                    </div>
                    <div class="labItems" style="display: flex">
                      <span v-for="(it, ind) in item.value" :key="ind">{{ it }}
                        <i @click="delFlavorLabel(index, ind)">X</i></span>
                      <div class="inputBox" :style="inputStyle" />
                    </div>
                    <span class="delFlavor delBut non" @click="delFlavor(item.name)">删除</span>
                  </div>
                </div>
                <div v-if="
                  !!leftDishFlavors.length &&
                  dishFlavors.length < dishFlavorsData.length
                " class="addBut" @click="addFlavore">
                  添加口味
                </div>
              </div>
            </div>
          </el-form-item>
        </el-form-item>
        <div>
          <el-form-item label="菜品图片:" prop="image">
            <image-upload :prop-image-url="imageUrl" @imageChange="imageChange">
              图片大小不超过2M<br>仅能上传 PNG JPEG JPG类型图片<br>建议上传200*200或300*300尺寸的图片
            </image-upload>
          </el-form-item>
        </div>
        <div class="address">
          <el-form-item label="菜品描述:" prop="region">
            <el-input v-model="ruleForm.description" type="textarea" :rows="3" maxlength="200"
              placeholder="菜品描述，最长200字" />
          </el-form-item>
        </div>
        <div class="subBox address">
          <el-button @click="() => $router.back()">
            取消
          </el-button>
          <el-button type="primary" :class="{ continue: actionType === 'add' }" @click="submitForm('ruleForm')">
            保存
          </el-button>
          <el-button v-if="actionType == 'add'" type="primary" @click="submitForm('ruleForm', 'goAnd')">
            保存并继续添加
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import HeadLable from '@/components/HeadLable/index.vue'
import SelectInput from './components/SelectInput.vue'
import ImageUpload from '@/components/ImgUpload/index.vue'
import {
  queryDishById,
  addDish,
  editDish,
  getCategoryList,
  commonDownload
} from '@/api/dish'
import { getShopId, getToken } from '@/utils/cookies'

export default defineComponent({
  name: 'AddDish',
  components: {
    HeadLable,
    SelectInput,
    ImageUpload
  },

  data() {
    return {
      restKey: 0,
      textarea: '',
      value: '',
      imageUrl: '',
      actionType: '' as string,
      dishList: [] as any[],
      dishFlavorsData: [] as any[],
      dishFlavors: [] as any[],
      leftDishFlavors: [] as any[],
      vueRest: '1',
      index: 0,
      inputStyle: { flex: 1 },
      headers: { token: getToken() },
      ruleForm: {
        name: '',
        id: '',
        price: '',
        code: '',
        image: '',
        description: '',
        dishFlavors: [] as any[],
        status: true,
        categoryId: '',
        shopId: getShopId()
      }
    }
  },

  computed: {
    rules() {
      return {
        name: [
          {
            required: true,
            validator: (rule: any, value: string, callback: Function) => {
              if (!value) {
                callback(new Error('请输入菜品名称'))
              } else {
                const reg = /^([A-Za-z0-9一-龥]){2,20}$/
                if (!reg.test(value)) {
                  callback(new Error('菜品名称输入不符，请输入2-20个字符'))
                } else {
                  callback()
                }
              }
            },
            trigger: 'blur'
          }
        ],
        categoryId: [
          { required: true, message: '请选择菜品分类', trigger: 'change' }
        ],
        image: {
          required: true,
          message: '菜品图片不能为空'
        },
        price: [
          {
            required: true,
            validator: (rules: any, value: string, callback: Function) => {
              const reg = /^([1-9]\d{0,5}|0)(\.\d{1,2})?$/
              if (!reg.test(value) || Number(value) <= 0) {
                callback(
                  new Error(
                    '菜品价格格式有误，请输入大于零且最多保留两位小数的金额'
                  )
                )
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ],
        code: [{ required: true, message: '请填写商品码', trigger: 'blur' }]
      }
    }
  },

  watch: {
    dishFlavors: {
      handler() {
        this.getLeftDishFlavors()
      },
      deep: true
    }
  },

  created() {
    this.getDishList()
    this.getFlavorListHand()
    this.actionType = (this.$route.query as any).id ? 'edit' : 'add'
    if ((this.$route.query as any).id) {
      this.init()
    }
  },

  methods: {
    getLeftDishFlavors() {
      let arr: any[] = []
      this.dishFlavorsData.map(item => {
        if (
          this.dishFlavors.findIndex(item1 => item.name === item1.name) === -1
        ) {
          arr.push(item)
        }
      })
      this.leftDishFlavors = arr
    },

    selectHandle(val: any, key: any, ind: any) {
      const arrDate = [...this.dishFlavors]
      const index = this.dishFlavorsData.findIndex(item => item.name === val)
      arrDate[key] = JSON.parse(JSON.stringify(this.dishFlavorsData[index]))
      this.dishFlavors = arrDate
    },

    async init() {
      queryDishById((this.$route.query as any).id).then(res => {
        if (res && res.data && res.data.code === 1) {
          const d = res.data.data
          this.ruleForm = { ...d }
          this.ruleForm.price = String(d.price)
          this.ruleForm.status = d.status == '1'
          this.dishFlavors =
            d.flavors &&
            d.flavors.map((obj: any) => ({
              ...obj,
              value: JSON.parse(obj.value)
            }))
          this.getLeftDishFlavors()
          this.imageUrl = d.image
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },

    addFlavore() {
      this.dishFlavors.push({ name: '', value: [] })
    },

    delFlavor(name: string) {
      let ind = this.dishFlavors.findIndex(item => item.name === name)
      this.dishFlavors.splice(ind, 1)
    },

    delFlavorLabel(index: number, ind: number) {
      this.dishFlavors[index].value.splice(ind, 1)
    },

    flavorPosition(index: number) {
      this.index = index
    },

    keyDownHandle(val: any) {
      if (event) {
        event.cancelBubble = true
        event.preventDefault()
        event.stopPropagation()
      }
      if (val.target.innerText.trim() != '') {
        this.dishFlavors[this.index].flavorData.push(val.target.innerText)
        val.target.innerText = ''
      }
    },

    getDishList() {
      getCategoryList({ type: 1, shopId: getShopId() }).then(res => {
        if (res.data.code === 1) {
          this.dishList = res && res.data && res.data.data
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },

    getFlavorListHand() {
      this.dishFlavorsData = [
        { name: '甜味', value: ['无糖', '少糖', '半糖', '多糖', '全糖'] },
        { name: '温度', value: ['热饮', '常温', '去冰', '少冰', '多冰'] },
        { name: '忌口', value: ['不要葱', '不要蒜', '不要香菜', '不要辣'] },
        { name: '辣度', value: ['不辣', '微辣', '中辣', '重辣'] }
      ]
    },

    submitForm(formName: any, st: any) {
      (this.$refs[formName] as any).validate((valid: any) => {
        if (valid) {
          if (!this.ruleForm.image) return this.$message.error('菜品图片不能为空')
          let params: any = { ...this.ruleForm }
          params.status =
            this.actionType === 'add' ? 0 : this.ruleForm.status ? 1 : 0
          params.categoryId = this.ruleForm.categoryId
          params.flavors = this.dishFlavors.map(obj => ({
            ...obj,
            value: JSON.stringify(obj.value)
          }))
          delete params.dishFlavors
          if (this.actionType == 'add') {
            delete params.id
            addDish(params)
              .then(res => {
                if (res.data.code === 1) {
                  this.$message.success('菜品添加成功！')
                  if (!st) {
                    this.$router.push({ path: '/dish' })
                  } else {
                    this.dishFlavors = []
                    this.imageUrl = ''
                    this.ruleForm = {
                      name: '', id: '', price: '', code: '', image: '',
                      description: '', dishFlavors: [], status: true,
                      categoryId: '', shopId: getShopId()
                    }
                    this.restKey++
                  }
                } else {
                  this.$message.error(res.data.desc || res.data.msg)
                }
              })
              .catch(err => {
                this.$message.error('请求出错了：' + err.message)
              })
          } else {
            delete params.createTime
            delete params.updateTime
            editDish(params)
              .then(res => {
                if (res && res.data && res.data.code === 1) {
                  this.$router.push({ path: '/dish' })
                  this.$message.success('菜品修改成功！')
                } else {
                  this.$message.error(res.data.desc || res.data.msg)
                }
              })
              .catch(err => {
                this.$message.error('请求出错了：' + err.message)
              })
          }
        } else {
          return false
        }
      })
    },

    imageChange(value: any) {
      this.ruleForm.image = value
    }
  }
})
</script>
<style lang="scss" scoped>
.addBrand-container {
  .el-form--inline .el-form-item__content {
    width: 293px;
  }

  .el-input {
    width: 350px;
  }

  .address {
    .el-form-item__content {
      width: 777px !important;
    }
  }

  .el-form-item[prop="image"] {
    .el-form-item__content {
      width: auto !important;
      min-width: 200px;
    }
  }
}
</style>
<style lang="scss" scoped>
.addBrand {
  &-container {
    margin: 30px;

    .container {
      position: relative;
      z-index: 1;
      background: #fff;
      padding: 30px;
      border-radius: 4px;
      min-height: 500px;

      .subBox {
        padding-top: 30px;
        text-align: center;
        border-top: solid 1px $gray-5;
      }

      .upload-item {
        .el-form-item__error {
          top: 90%;
        }
      }
    }
  }
}

.flavorBox {
  width: 777px;

  .addBut {
    background: #c3bef0;
    display: inline-block;
    padding: 0px 20px;
    border-radius: 3px;
    line-height: 40px;
    cursor: pointer;
    border-radius: 4px;
    color: #333333;
    font-weight: 500;
  }

  .flavor {
    border: solid 1px #dfe2e8;
    border-radius: 3px;
    padding: 15px;
    background: #fafafb;

    .title {
      color: #606168;

      .des-box {
        padding-left: 44px;
      }
    }

    .cont {
      .items {
        display: flex;
        margin: 10px 0;

        .itTit {
          width: 150px;
          margin-right: 15px;

          input {
            width: 100%;
          }
        }

        .labItems {
          flex: 1;
          display: flex;
          flex-wrap: wrap;
          border-radius: 3px;
          min-height: 39px;
          border: solid 1px #d8dde3;
          background: #fff;
          padding: 0 5px;

          span {
            display: inline-block;
            color: #ffc200;
            margin: 5px;
            line-height: 26px;
            padding: 0 10px;
            background: #fffbf0;
            border: 1px solid #fbe396;
            border-radius: 4px;
            font-size: 12px;

            i {
              cursor: pointer;
              font-style: normal;
            }
          }

          .inputBox {
            display: inline-block;
            width: 100%;
            height: 36px;
            line-height: 36px;
            overflow: hidden;
          }
        }

        .delFlavor {
          display: inline-block;
          padding: 0 10px;
          color: #f19c59;
          cursor: pointer;
        }
      }
    }
  }
}
</style>
