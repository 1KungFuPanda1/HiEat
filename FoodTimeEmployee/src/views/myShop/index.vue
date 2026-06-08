<template>
  <div class="shop-info-container">
    <el-card class="box-card shop-card">
      <template #header>
        <div class="card-header">
          <h2 class="homeTitle">店铺信息</h2>
          <el-button v-if="!isEditing" type="primary" icon="el-icon-edit" @click="startEditing">修改信息</el-button>
        </div>
      </template>

      <div class="shop-info-content">
        <div class="info-section basic-info">
          <h3 class="section-title"><i class="el-icon-s-shop"></i> 基本信息</h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="info-label">店铺名称</span>
                <span class="info-value">{{ shopInfo.shopName }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="info-label">联系电话</span>
                <span class="info-value">{{ shopInfo.phone }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="info-label">起送费用</span>
                <span class="info-value">¥{{ displayMinFee }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="info-label">配送费</span>
                <span class="info-value">¥{{ displayDeliverFee }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="info-section detail-info">
          <h3 class="section-title"><i class="el-icon-location-information"></i> 详细信息</h3>
          <div class="info-item">
            <span class="info-label">店铺地址</span>
            <span class="info-value">{{ shopInfo.address }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">店主姓名</span>
            <span class="info-value">{{ shopInfo.owner }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">店铺简介</span>
            <span class="info-value description">{{ shopInfo.des }}</span>
          </div>
        </div>

        <div class="info-section image-info">
          <h3 class="section-title"><i class="el-icon-picture"></i> 图片信息</h3>
          <el-row :gutter="40">
            <el-col :span="12">
              <div class="info-item image-item">
                <span class="info-label">店铺Logo</span>
                <div class="image-container">
                  <img v-if="shopInfo.image" :src="shopInfo.image" class="shop-logo" @click="previewImage(shopInfo.image)" />
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item image-item">
                <span class="info-label">营业执照</span>
                <div class="image-container">
                  <img v-if="shopInfo.businessLicense" :src="shopInfo.businessLicense" class="license-image" @click="previewImage(shopInfo.businessLicense)" />
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="info-section shop-photos">
          <h3 class="section-title"><i class="el-icon-camera"></i> 店铺照片</h3>
          <div class="shop-photos-container">
            <div v-if="shopInfo.shopImages && shopInfo.shopImages.length > 0" class="photos-list">
              <div v-for="(photo, index) in shopInfo.shopImages" :key="index" class="photo-item">
                <img :src="photo" @click="previewImage(photo)" class="shop-photo" />
              </div>
            </div>
            <div v-else class="no-photos">
              <i class="el-icon-picture-outline"></i>
              <p>暂无店铺照片</p>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 修改信息对话框 -->
    <el-dialog title="修改店铺信息" v-model="editDialogVisible" width="50%" center>
      <el-form :model="editForm" :rules="rules" ref="editForm" label-width="120px" label-position="left">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="店铺名称" prop="shopName">
              <el-input v-model="editForm.shopName" placeholder="请输入店铺名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="editForm.phone" placeholder="请输入联系电话"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="起送费用" prop="minFee">
              <el-input-number v-model="displayEditMinFee" :min="0" :precision="2" :step="1" :controls="true"
                style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配送费" prop="deliverFee">
              <el-input-number v-model="displayEditDeliverFee" :min="0" :precision="2" :step="1" :controls="true"
                style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="店铺地址" prop="address">
          <el-input v-model="editForm.address" placeholder="请输入店铺地址"></el-input>
        </el-form-item>
        <el-form-item label="店铺简介" prop="description">
          <el-input type="textarea" :rows="3" v-model="editForm.des" placeholder="请输入店铺简介"></el-input>
        </el-form-item>
        <el-row :gutter="40">
          <el-col :span="12">
            <el-form-item label="店铺Logo" prop="logo">
              <div class="dialog-image-container">
                <el-upload class="avatar-uploader" action="/api/common/upload" :show-file-list="false"
                  :on-success="handleEditLogoSuccess" :before-upload="beforeLogoUpload" :headers="headers">
                  <img v-if="editForm.image" :src="editForm.image" class="dialog-shop-logo" />
                  <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="营业执照" prop="businessLicense">
              <div class="dialog-image-container">
                <el-upload class="avatar-uploader" action="/api/common/upload" :show-file-list="false"
                  :on-success="handleEditLicenseSuccess" :before-upload="beforeLicenseUpload" :headers="headers">
                  <img v-if="editForm.businessLicense" :src="editForm.businessLicense" class="dialog-shop-logo" />
                  <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="店铺照片">
          <div class="shop-photos-edit">
            <div class="photos-list">
              <div v-for="(photo, index) in editForm.shopImages" :key="index" class="photo-item">
                <img :src="photo" class="shop-photo" />
                <div class="photo-actions">
                  <el-button type="danger" icon="el-icon-delete" circle size="mini" @click="removeShopPhoto(index)"></el-button>
                </div>
              </div>
              <div class="photo-upload-item">
                <el-upload action="/api/common/upload" :show-file-list="false" :on-success="handleShopPhotoSuccess"
                  :before-upload="beforePhotoUpload" :headers="headers">
                  <div class="upload-trigger">
                    <i class="el-icon-plus"></i>
                    <span>添加照片</span>
                  </div>
                </el-upload>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelEdit">取 消</el-button>
          <el-button type="primary" @click="submitEdit">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="imagePreviewVisible" width="50%" class="image-preview-dialog">
      <img :src="previewImageUrl" class="preview-image" />
    </el-dialog>
  </div>
</template>

<script lang="ts" src="./index.ts"></script>
<style lang="scss" scoped src="./index.scss"></style>
