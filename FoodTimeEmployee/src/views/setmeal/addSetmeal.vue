<template>
  <div class="addBrand-container">
    <div class="container">
      <el-form ref="ruleForm" :model="ruleForm" :rules="rules" :inline="true" label-width="180px" class="demo-ruleForm">
        <div>
          <el-form-item label="套餐名称:" prop="name">
            <el-input v-model="ruleForm.name" placeholder="请填写套餐名称" maxlength="14" />
          </el-form-item>
          <el-form-item label="套餐分类:" prop="idType">
            <el-select v-model="ruleForm.idType" placeholder="请选择套餐分类" @change="$forceUpdate()">
              <el-option v-for="(item, index) in setMealList" :key="index" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </div>

        <div>
          <el-form-item label="套餐价格:" prop="price">
            <el-input v-model="ruleForm.price" placeholder="请设置套餐价格" />
          </el-form-item>
        </div>
        <div>
          <el-form-item label="套餐菜品:" required>
            <el-form-item>
              <div class="addDish">
                <span v-if="dishTable.length == 0" class="addBut" @click="openAddDish('new')">
                  + 添加菜品</span>
                <div v-if="dishTable.length != 0" class="content">
                  <div class="addBut" style="margin-bottom: 20px" @click="openAddDish('change')">
                    + 添加菜品
                  </div>
                  <div class="table">
                    <el-table :data="dishTable" style="width: 100%">
                      <el-table-column prop="name" label="名称" width="180" align="center" />
                      <el-table-column prop="price" label="原价" width="180" align="center">
                        <template v-slot="{ row }">
                          {{ (Number(row.price).toFixed(2) * 100) / 100 }}
                        </template>
                      </el-table-column>
                      <el-table-column prop="address" label="份数" align="center">
                        <template v-slot="{ row }">
                          <el-input-number v-model="row.copies" size="small" :min="1" :max="99" label="描述文字" />
                        </template>
                      </el-table-column>
                      <el-table-column prop="address" label="操作" width="180px;" align="center">
                        <template v-slot="{ row, $index }">
                          <el-button type="text" size="small" class="delBut non" @click="delDishHandle($index)">
                            删除
                          </el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-form-item>
        </div>
        <div>
          <el-form-item label="套餐图片:" required prop="image">
            <image-upload :prop-image-url="imageUrl" @imageChange="imageChange">
              图片大小不超过2M<br>仅能上传 PNG JPEG JPG类型图片<br>建议上传200*200或300*300尺寸的图片
            </image-upload>
          </el-form-item>
        </div>
        <div class="address">
          <el-form-item label="套餐描述:">
            <el-input v-model="ruleForm.description" type="textarea" :rows="3" maxlength="200"
              placeholder="套餐描述，最长200字" />
          </el-form-item>
        </div>
        <div class="subBox address">
          <el-form-item>
            <el-button @click="() => $router.back()">
              取消
            </el-button>
            <el-button type="primary" :class="{ continue: actionType === 'add' }"
              @click="submitForm('ruleForm', false)">
              保存
            </el-button>
            <el-button v-if="actionType == 'add'" type="primary" @click="submitForm('ruleForm', true)">
              保存并继续添加
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
    <el-dialog v-if="dialogVisible" title="添加菜品" class="addDishList" v-model="dialogVisible" width="60%"
      :before-close="handleClose">
      <el-input v-model="value" class="seachDish" placeholder="请输入菜品名称进行搜索" style="width: 293px; height: 40px"
        size="small" clearable>
        <template #prefix>
          <i class="el-input__icon el-icon-search" style="cursor: pointer" @click="seachHandle" />
        </template>
      </el-input>
      <AddDish v-if="dialogVisible"
       ref="adddish"
      :check-list="checkList"
      :seach-key="seachKey"
      :dish-list="dishList"
      @checkList="getCheckList" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">取 消</el-button>
          <el-button type="primary" @click="addTableList">添 加</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import HeadLable from '@/components/HeadLable/index.vue'
import ImageUpload from '@/components/ImgUpload/index.vue'
import AddDish from './components/AddDish.vue'
import { querySetmealById, addSetmeal, editSetmeal } from '@/api/setMeal'
import { getCategoryList } from '@/api/dish'
import { getShopId } from '@/utils/cookies'

export default defineComponent({
  name: 'AddSetmeal',
  components: {
    HeadLable,
    AddDish,
    ImageUpload
  },

  data() {
    return {
      value: '',
      setMealList: [] as any[],
      seachKey: '',
      dishList: [] as any[],
      imageUrl: '',
      actionType: '' as string,
      dishTable: [] as any[],
      dialogVisible: false,
      checkList: [] as any[],
      ruleForm: {
        shopId: getShopId(),
        name: '',
        categoryId: '',
        price: '',
        code: '',
        image: '',
        description: '',
        dishList: [] as any[],
        status: true,
        idType: ''
      }
    }
  },

  computed: {
    rules() {
      return {
        name: {
          required: true,
          validator: (rule: any, value: string, callback: Function) => {
            if (!value) {
              callback(new Error('请输入套餐名称'))
            } else {
              const reg = /^([A-Za-z0-9一-龥]){2,20}$/
              if (!reg.test(value)) {
                callback(new Error('套餐名称输入不符，请输入2-20个字符'))
              } else {
                callback()
              }
            }
          },
          trigger: 'blur'
        },
        idType: {
          required: true,
          message: '请选择套餐分类',
          trigger: 'change'
        },
        image: {
          required: true,
          message: '菜品图片不能为空'
        },
        price: {
          required: true,
          validator: (rules: any, value: string, callback: Function) => {
            const reg = /^([1-9]\d{0,5}|0)(\.\d{1,2})?$/
            if (!reg.test(value) || Number(value) <= 0) {
              callback(
                new Error(
                  '套餐价格格式有误，请输入大于零且最多保留两位小数的金额'
                )
              )
            } else {
              callback()
            }
          },
          trigger: 'blur'
        },
        code: { required: true, message: '请输入商品码', trigger: 'blur' }
      }
    }
  },

  created() {
    this.getDishTypeList()
    this.actionType = (this.$route.query as any).id ? 'edit' : 'add'
    if (this.actionType == 'edit') {
      this.init()
    }
  },

  methods: {
    async init() {
      querySetmealById((this.$route.query as any).id).then(res => {
        if (res && res.data && res.data.code === 1) {
          this.ruleForm = res.data.data
          this.ruleForm.status = res.data.data.status == '1'
          ;(this.ruleForm as any).price = res.data.data.price
          this.imageUrl = res.data.data.image
          this.checkList = res.data.data.setmealDishes
          this.dishTable = res.data.data.setmealDishes.reverse()
          this.ruleForm.idType = res.data.data.categoryId
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },

    seachHandle() {
      this.seachKey = this.value
    },

    getDishTypeList() {
      getCategoryList({ type: 2, page: 1, pageSize: 1000, shopId: getShopId() }).then(res => {
        if (res && res.data && res.data.code === 1) {
          this.setMealList = res.data.data.map((obj: any) => ({
            ...obj,
            idType: obj.id
          }))
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },

    delDishHandle(index: any) {
      this.dishTable.splice(index, 1)
      this.checkList = this.dishTable
    },

    getCheckList(value: any) {
      this.checkList = [...value].reverse()
    },

    openAddDish(st: string) {
      this.seachKey = ''
      this.dialogVisible = true
    },

    handleClose(done: any) {
      this.dialogVisible = false
      this.checkList = JSON.parse(JSON.stringify(this.dishTable))
    },

    addTableList() {
      this.dishTable = JSON.parse(JSON.stringify(this.checkList))
      this.dishTable.forEach((n: any) => {
        n.copies = 1
      })
      this.dialogVisible = false
    },

    submitForm(formName: any, st: any) {
      (this.$refs[formName] as any).validate((valid: any) => {
        if (valid) {
          if (this.dishTable.length === 0) {
            return this.$message.error('套餐下菜品不能为空')
          }
          if (!this.ruleForm.image) return this.$message.error('套餐图片不能为空')

          let prams = { ...this.ruleForm } as any
          prams.setmealDishes = this.dishTable.map((obj: any) => ({
            copies: obj.copies,
            dishId: obj.dishId,
            name: obj.name,
            price: obj.price
          }))
          prams.status =
            this.actionType === 'add' ? 0 : this.ruleForm.status ? 1 : 0
          prams.categoryId = this.ruleForm.idType
          if (this.actionType == 'add') {
            delete prams.id
            addSetmeal(prams)
              .then(res => {
                if (res && res.data && res.data.code === 1) {
                  this.$message.success('套餐添加成功！')
                  if (!st) {
                    this.$router.push({ path: '/setmeal' })
                  } else {
                    (this as any).$refs.ruleForm.resetFields()
                    this.dishList = []
                    this.dishTable = []
                    this.ruleForm = {
                      name: '', categoryId: '', price: '', code: '', image: '',
                      description: '', dishList: [], status: true, id: '', idType: '', shopId: getShopId()
                    } as any
                    this.imageUrl = ''
                  }
                } else {
                  this.$message.error(res.data.msg)
                }
              })
              .catch(err => {
                this.$message.error('请求出错了：' + err.message)
              })
          } else {
            delete prams.updateTime
            editSetmeal(prams)
              .then(res => {
                if (res.data.code === 1) {
                  this.$message.success('套餐修改成功！')
                  this.$router.push({ path: '/setmeal' })
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
<style>
.avatar-uploader .el-icon-plus:after {
  position: absolute;
  display: inline-block;
  content: ' ' !important;
  left: calc(50% - 20px);
  top: calc(50% - 40px);
  width: 40px;
  height: 40px;
  background: url('./../../assets/icons/icon_upload@2x.png') center center no-repeat;
  background-size: 20px;
}
</style>
<style lang="scss">
.addBrand-container {
  .avatar-uploader .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }

  .avatar-uploader .el-upload:hover {
    border-color: #ffc200;
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 200px;
    height: 160px;
    line-height: 160px;
    text-align: center;
  }

  .avatar {
    width: 200px;
    height: 160px;
    display: block;
  }

  .el-input {
    width: 293px;
  }

  .address {
    .el-form-item__content {
      width: 777px !important;
    }
  }

  .el-input__prefix {
    top: 2px;
  }

  .addDish {
    .el-input {
      width: 130px;
    }

    .el-input-number__increase {
      border-left: solid 1px #fbe396;
      background: #fffbf0;
    }

    .el-input-number__decrease {
      border-right: solid 1px #fbe396;
      background: #fffbf0;
    }

    input {
      border: 1px solid #fbe396;
    }

    .table {
      border: solid 1px #ebeef5;
      border-radius: 3px;

      th {
        padding: 5px 0;
      }

      td {
        padding: 7px 0;
      }
    }
  }

  .addDishList {
    .seachDish {
      position: absolute;
      top: 12px;
      right: 20px;
    }

    .el-dialog__footer {
      padding-top: 27px;
    }

    .el-dialog__body {
      padding: 0;
      border-bottom: solid 1px #efefef;
    }

    .seachDish {
      .el-input__inner {
        height: 40px;
        line-height: 40px;
      }
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

      .el-input {
        width: 350px;
      }

      .addDish {
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

        .content {
          background: #fafafb;
          padding: 20px;
          border: solid 1px #d8dde3;
          border-radius: 3px;
        }
      }
    }
  }
}
</style>
