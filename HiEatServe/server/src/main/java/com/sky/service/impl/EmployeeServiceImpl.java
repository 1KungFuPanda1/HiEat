package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.UpdatePasswordDTO;
import com.sky.dto.admin.employee.ShopRegisterDTO;
import com.sky.entity.Employee;
import com.sky.entity.Shop;
import com.sky.entity.ShopImage;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.ShopImageMapper;
import com.sky.mapper.ShopMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private ShopImageMapper shopImageMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        // 1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        // 2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            // 账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码比对
        // 对前端传过来的明文密码进行md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            // 密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            // 账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        // 对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        // 获取当前登录员工的 shop_id，确保新增员工关联到正确的店铺
        Long currentEmpId = BaseContext.getCurrentId();
        Employee currentEmp = employeeMapper.getById(currentEmpId);
        if (currentEmp != null && currentEmp.getShopId() != null) {
            employee.setShopId(currentEmp.getShopId());
        }

        // 设置账号的状态，默认正常状态 1表示正常 0表示锁定
        employee.setStatus(StatusConstant.ENABLE);

        // 设置密码，默认密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置默认不是商家
        employee.setIsManager(StatusConstant.DISABLE);
        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // select * from employee limit 0,10
        // 开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        // update employee set status = ? where id = ?

        /*
         * Employee employee = new Employee();
         * employee.setStatus(status);
         * employee.setId(id);
         */

        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工
     *
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    /**
     * 编辑员工信息
     *
     * @param employeeDTO
     */
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        employeeMapper.update(employee);
    }

    /**
     * 商家注册
     * 
     * @param shopRegisterDTO
     * @return
     */
    @Override
    @Transactional
    public Employee register(ShopRegisterDTO shopRegisterDTO) {
        // 检查用户名是否已存在
        Employee existEmployee = employeeMapper.getByUsername(shopRegisterDTO.getUsername());
        if (existEmployee != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 1. 创建店铺
        Shop shop = Shop.builder()
                .shopName(shopRegisterDTO.getShopName())
                .address(shopRegisterDTO.getShopAddress())
                .phone(shopRegisterDTO.getPhone())
                .image(shopRegisterDTO.getShopLogo())
                .categoryId(shopRegisterDTO.getCategoryId())
                .businessLicense(shopRegisterDTO.getBusinessLicense()) // 添加营业执照
                .status(StatusConstant.DISABLE) // 初始状态为禁用
                .auditStatus(0) // 待审核状态
                .score(5.0)
                .averageSendTime(30)
                .deliverFee(0)
                .minFee(0)
                .orderQuantity(0)
                .des("店主很懒，还没设置简介~")
                .createTime(LocalDateTime.now())
                .build();

        // 保存店铺信息
        shopMapper.insert(shop);

        // 保存店铺照片
//        [
//         ShopImage(1001, img1.jpg),
//         ShopImage(1001, img2.jpg),
//         ShopImage(1001, img3.jpg)
//        ]

        ArrayList<ShopImage> shopImages = new ArrayList<>();
        for (String shopPhoto : shopRegisterDTO.getShopPhotos()) {
            ShopImage shopImage = new ShopImage();
            shopImage.setShopId(shop.getId());
            shopImage.setImageUrl(shopPhoto);
            shopImages.add(shopImage);
        }
        shopImageMapper.insertBatch(shopImages);

        // 2. 创建员工（店长）
        Employee employee = new Employee();
        BeanUtils.copyProperties(shopRegisterDTO, employee);

        // 设置店铺ID
        employee.setShopId(shop.getId());

        // 设置为店长
        employee.setIsManager(1);

        // 设置账号状态为禁用（待审核）
        employee.setStatus(StatusConstant.DISABLE);

        employee.setSex("1");

        // 设置密码加密
        employee.setPassword(DigestUtils.md5DigestAsHex(shopRegisterDTO.getPassword().getBytes()));

        // 设置创建时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 保存员工信息
        employeeMapper.insert(employee);

        return employee;
    }

    @Override
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        Long currentId = BaseContext.getCurrentId();
        Employee employee = employeeMapper.getById(currentId);
        if (!DigestUtils.md5DigestAsHex(updatePasswordDTO.getOldPassword().getBytes()).equals(employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        employee.setPassword(DigestUtils.md5DigestAsHex(updatePasswordDTO.getNewPassword().getBytes()));
        employeeMapper.update(employee);
    }
}
