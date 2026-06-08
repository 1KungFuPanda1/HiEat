<template>
  <div class="addBrand-container">
    <HeadLable :title="title" :goback="true" />
    <div class="container">
      <el-form ref="ruleForm" :model="ruleForm" :rules="rules" :inline="false" label-width="180px" class="demo-ruleForm">
        <el-form-item label="账号:" prop="username">
          <el-input v-model="ruleForm.username" placeholder="请输入账号" maxlength="20" />
        </el-form-item>
        <el-form-item label="员工姓名:" prop="name">
          <el-input v-model="ruleForm.name" placeholder="请输入员工姓名" maxlength="12" />
        </el-form-item>
        <el-form-item label="手机号:" prop="phone">
          <el-input v-model="ruleForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="性别:" prop="sex">
          <el-radio-group v-model="ruleForm.sex">
            <el-radio label="男" />
            <el-radio label="女" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号:" prop="idNumber" class="idNumber">
          <el-input v-model="ruleForm.idNumber" placeholder="请输入身份证号" maxlength="20" />
        </el-form-item>
        <div class="subBox address">
          <el-button @click="() => $router.push('/employee')">取消</el-button>
          <el-button type="primary" :class="{ continue: actionType === 'add' }" @click="submitForm('ruleForm', false)">
            保存
          </el-button>
          <el-button v-if="actionType == 'add'" type="primary" @click="submitForm('ruleForm', true)">
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
import { queryEmployeeById, addEmployee, editEmployee } from '@/api/employee'
import { getShopId } from '@/utils/cookies'

export default defineComponent({
  name: 'AddEmployee',
  components: { HeadLable },

  data() {
    const isCellPhone = (val: any) => /^1(3|4|5|6|7|8)\d{9}$/.test(val)

    const checkphone = (_rule: any, value: any, callback: any) => {
      if (!value) {
        callback(new Error('请输入手机号'))
      } else if (!isCellPhone(value)) {
        callback(new Error('请输入正确的手机号!'))
      } else {
        callback()
      }
    }

    const validID = (_rule: any, value: any, callback: any) => {
      let reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
      if (!value) {
        callback(new Error('请输入身份证号码'))
      } else if (reg.test(value)) {
        callback()
      } else {
        callback(new Error('身份证号码不正确'))
      }
    }

    return {
      title: '添加员工',
      actionType: '' as string,
      shopId: '',
      ruleForm: {
        name: '',
        phone: '',
        shopId: '',
        sex: '男',
        idNumber: '',
        username: ''
      },
      rules: {
        name: [
          {
            required: true,
            validator: (_rule: any, value: string, callback: Function) => {
              if (!value) {
                callback(new Error('请输入员工姓名'))
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ],
        username: [
          {
            required: true,
            validator: (_rule: any, value: string, callback: Function) => {
              if (!value) {
                callback(new Error('请输入账号'))
              } else {
                const reg = /^([a-z]|[0-9]){3,20}$/
                if (!reg.test(value)) {
                  callback(new Error('账号输入不符，请输入3-20个字符'))
                } else {
                  callback()
                }
              }
            },
            trigger: 'blur'
          }
        ],
        phone: [{ required: true, validator: checkphone, trigger: 'blur' }],
        idNumber: [{ required: true, validator: validID, trigger: 'blur' }]
      }
    }
  },

  created() {
    this.shopId = this.ruleForm.shopId = getShopId()
    this.actionType = this.$route.query.id ? 'edit' : 'add'
    if (this.$route.query.id) {
      this.title = '修改员工信息'
      this.init()
    }
  },

  methods: {
    async init() {
      const id = this.$route.query.id as string
      queryEmployeeById(id).then((res: any) => {
        if (res.data.code === 1) {
          this.ruleForm = res.data.data
          this.ruleForm.sex = res.data.data.sex === '0' ? '女' : '男'
        } else {
          this.$message.error(res.data.msg)
        }
      })
    },

    submitForm(formName: any, st: any) {
      (this.$refs[formName] as any).validate((valid: any) => {
        if (valid) {
          if (this.actionType === 'add') {
            const params = {
              ...this.ruleForm,
              sex: this.ruleForm.sex === '女' ? '0' : '1'
            }
            addEmployee(params)
              .then((res: any) => {
                if (res.data.code === 1) {
                  this.$message.success('员工添加成功！')
                  if (!st) {
                    this.$router.push({ path: '/employee' })
                  } else {
                    this.ruleForm = {
                      username: '', name: '', phone: '', shopId: '',
                      sex: '男', idNumber: ''
                    }
                  }
                } else {
                  this.$message.error(res.data.msg)
                }
              })
          } else {
            const params = {
              ...this.ruleForm,
              sex: this.ruleForm.sex === '女' ? '0' : '1'
            }
            editEmployee(params)
              .then((res: any) => {
                if (res.data.code === 1) {
                  this.$message.success('员工信息修改成功！')
                  this.$router.push({ path: '/employee' })
                } else {
                  this.$message.error(res.data.msg)
                }
              })
          }
        }
      })
    }
  }
})
</script>

<style lang="scss" scoped>
.addBrand-container {
  margin: 30px;
  margin-top: 0px;

  .HeadLable {
    background-color: transparent;
    margin-bottom: 0px;
    padding-left: 0px;
  }

  .container {
    position: relative;
    z-index: 1;
    background: #fff;
    padding: 30px;
    border-radius: 4px;

    .subBox {
      padding-top: 30px;
      text-align: center;
      border-top: solid 1px $gray-5;
    }
  }

  .idNumber { margin-bottom: 39px; }
  .el-form-item { margin-bottom: 29px; }
  .el-input { width: 293px; }
}
</style>
