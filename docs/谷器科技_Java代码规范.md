# 谷器科技 Java 代码规范

> 本文档依据阿里《Java 开发手册（泰山版）》整理，作为团队内部 Java 编码与数据库设计的统一约定。
> 条目以 **【强制】** / **【推荐】** 标注约束级别：强制项须在代码评审中通过，推荐项视场景取舍。

## 目录

- [一、命名规范](#一命名规范)
- [二、代码格式](#二代码格式)
- [三、OOP 规约](#三oop-规约)
- [四、线程处理](#四线程处理)
- [五、控制语句](#五控制语句)
- [六、注释](#六注释)
- [七、SQL 要求](#七sql-要求)
- [八、数据库 MySQL](#八数据库-mysql)
- [九、经验积累（优化方向）](#九经验积累优化方向)

---

## 一、命名规范

| 名称 | 执行 | 规范 | 示例 |
| --- | --- | --- | --- |
| Java 类、接口、枚举 | 强制 | 由英文单词组成，首字母大写，多个字母采用驼峰形式。<br>禁止用汉语拼音或汉语拼音首字母，需用对应的英文字母。<br>禁止出现特殊字符（`!@#$%^()-_><：;'"`），禁止出现数字。 | 正：`TestMethod`<br>反：`CeShiFangFa`（汉语拼音）<br>反：`CsFf`（汉语拼音首写字母）<br>反：`TM`（英文首写字母）<br>反：`TestMethod2`（不许带数字）<br>反：`TestMethod#`（特殊字符） |
| Java 方法名、参数名、成员变量、局部变量 | 强制 | 由英文单词组成，首字母大写，多个字母采用驼峰形式。<br>禁止用汉语拼音或汉语拼音首字母，需用对应的英文字母。<br>禁止出现特殊字符（`!@#$%^()-_><：;'"`）。 | 正：`testMethod`、`testMethod()`<br>反：`ceShiFangFa`（汉语拼音）<br>反：`csFf`（汉语拼音首写字母）<br>反：`tM`（英文首写字母）<br>反：`TestMethod#`（特殊字符） |
| 常量命名 | 强制 | 常量命名全部大写，单词间用下划线隔开，力求语义表达完整清楚，不要嫌名字长。<br>由英文单词组成。<br>禁止出现特殊字符（`!@#$%^()-_><：;'"`）。<br>禁止用汉语拼音或汉语拼音首字母。 | 正：`TEST_METHOD`<br>反：`CS_FF`（汉语拼音首写字母）<br>反：`T_M`（英文首写字母）<br>反：`TEST_METHOD#`（特殊字符） |
| 抽象类命名 | 可选 | 抽象类命名使用 `Abstract` 或 `Base` 开头。 | `AbstractTest` |
| 异常类命名 | 可选 | 命名使用 `Exception` 结尾。 | `MethodException` |
| 单元测试类命名 | 可选 | 命名使用 `Test` 结尾。 | `MethodTest` |
| POJO 类中的任何布尔类型的变量 | 强制 | 不要加 `is` 前缀，否则部分框架解析会引起序列化错误。 | 说明：在 MySQL 规约中的建表约定第一条，表达是与否的值采用 `is_xxx` 的命名方式，所以需要在 `<resultMap>` 设置从 `is_xxx` 到 `xxx` 的映射关系。<br>反例：定义为基本数据类型 `Boolean isDeleted` 的属性，它的方法也是 `isDeleted()`，框架在反向解析的时候，"误以为"对应的属性名称是 `deleted`，导致属性获取不到，进而抛出异常。 |
| 包名 | 可选 | 统一使用小写，点分隔符之间有且仅有一个自然语义的英语单词。包名统一使用单数形式，但是类名如果有复数含义，类名可以使用复数形式。 | — |
| 接口和实现类的命名 | 强制 | 对于 Service 和 DAO 类，基于 SOA 的理念，暴露出来的服务一定是接口，内部的实现类用 `Impl` 的后缀与接口区别。 | 正例：`CacheServiceImpl` 实现 `CacheService` 接口。 |
| 枚举类名 | 强制 | 枚举类名带上 `Enum` 后缀，枚举成员名称需要全大写，单词间用下划线隔开。<br>说明：枚举其实就是特殊的常量类，且构造方法被默认强制是私有。 | 枚举名字为 `ProcessStatusEnum` 的成员名称：`SUCCESS` / `UNKNOWN_REASON`。 |

## 二、代码格式

**大括号**

- 左大括号前不换行。
- 左大括号后换行。
- 右大括号前换行。
- 右大括号后还有 `else` 等代码则不换行；表示终止的右大括号后必须换行。

> 注：原文档此节仅包含"大括号"一项规则。

## 三、OOP 规约

**【强制】** 避免通过一个类的对象引用访问此类的静态变量或静态方法，无谓增加编译器解析成本，直接用类名来访问即可。

**【强制】** 所有的覆写方法，必须加 `@Override` 注解。

> 说明：`getObject()` 与 `get0bject()` 的问题。一个是字母的 O，一个是数字的 0，加 `@Override` 可以准确判断是否覆盖成功。另外，如果在抽象类中对方法签名进行修改，其实现类会马上编译报错。

**【强制】** Object 的 `equals` 方法容易抛空指针异常，应使用常量或确定有值的对象来调用 `equals`。

```java
// 正例
"test".equals(object);

// 反例
object.equals("test");
```

**【强制】** 定义数据对象 DO 类时，属性类型要与数据库字段类型相匹配。

- 正例：数据库字段的 `bigint` 必须与类属性的 `Long` 类型相对应。
- 反例：某个案例的数据库表 `id` 字段定义类型 `bigint unsigned`，实际类对象属性为 `Integer`，随着 `id` 越来越大，超过 `Integer` 的表示范围而溢出成为负数。

**【强制】** 序列化类新增属性时，请不要修改 `serialVersionUID` 字段，避免反序列失败；如果完全不兼容升级，避免反序列化混乱，那么请修改 `serialVersionUID` 值。

> 说明：注意 `serialVersionUID` 不一致会抛出序列化运行时异常。

## 四、线程处理

**【强制】** 创建线程或线程池时请指定有意义的线程名称，方便出错时回溯。

```java
public class MyThread extends Thread {

    @Override
    public void run() {

    }

    public static void main(String[] args) {
        new Thread(new MyThread(), "Thread-1");
    }
}
```

![线程命名：构造 Thread 时显式指定线程名称](assets/image1.png)

**【强制】** `SimpleDateFormat` 是线程不安全的类，一般不要定义为 static 变量，如果定义为 static，必须加锁，或者使用 `DateUtils` 工具类。

**【强制】** 必须回收自定义的 `ThreadLocal` 变量，尤其在线程池场景下，线程经常会被复用，如果不清理自定义的 `ThreadLocal` 变量，可能会影响后续业务逻辑和造成内存泄露等问题。尽量在代理中使用 `try-finally` 块进行回收。

## 五、控制语句

**【强制】** 在一个 `switch` 块内，都必须包含一个 `default` 语句并且放在最后，即使它什么代码也没有。

**【强制】** 当 `switch` 括号内的变量类型为 `String` 并且此变量为外部参数时，必须先进行 `null` 判断，否则会报错。

**【强制】** 对外接口限制：对外提供的接口需要限制访问数量，或做异步处理，防止被爆掉内存或端口拥堵。

## 六、注释

### 6.1 类、接口、枚举注释

配置路径：`File` → `Settings` → `File and Code Templates` → （Class、Interface、Enum）建立模板。模板主要包含类名、建立者、类业务作用、创建时间。

```java
#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end
#parse("File Header.java")
/**
*@ClassName ${NAME}
*@Description TODO
*@author YangJingBo
*@date ${DATE} ${TIME}
*/
public class ${NAME} {
}
```

![File and Code Templates 配置入口](assets/image2.png)

### 6.2 方法级别注释

配置路径：`File` → `Settings` → `Live Templates`。

方法模板：

```java
/**
 *@Description
 * @Author YangJingBO
 * @date $date$ $time$
 *@param $param$
 *@Return $return$
 */
```

操作步骤：① 添加模板；② 模板命名；③ 添加以上模板；④ 配置变量。配置完成后，在类的方法上敲入模板命名再回车，就能引入模板。

![Live Templates 新增模板（1 添加、2 命名、3 模板内容、4 配置变量）](assets/image3.png)

![Editable Template Variables：time=time()、return=methodReturnType()、param=methodParameterTypes()、date=date()](assets/image4.png)

![在方法内部敲入模板简写并回车，即可生成注释内容](assets/image5.png)

模板配置完成后，需要在方法内部敲模板，回车后生成方法注释，然后拷出到方法上。

### 6.3 方法与行内注释

新增代码时添加注释，注明添加人、添加时间与业务逻辑说明：

```java
// add 添加姓名 添加时间 业务逻辑说明
```

修改代码时添加注释，注明修改人、修改时间与修改说明：

```java
//modify 修改人姓名 修改时间20210818 修改说明
```

## 七、SQL 要求

**【强制】** 用户禁止字符串拼接 SQL 访问数据库，防止 SQL 注入，`$` 属于字符串拼接。

**【推荐】** SQL 尽量减少 `order by`，这样会降低查询速率。

**【推荐】** 尽量减少多表联合查询（`left join`、`inner join` 等），会降低查询效率，建议进行多次查询，在程序里进行数据拼装。

## 八、数据库 MySQL

### 8.1 MySQL 基本设定

**【强制】** 字符集选择 UTF8（建库时确定），设置如下：

```sql
DEFAULT_CHARSET = utf8
DEFAULT_COLLATION = utf8_general_ci
```

字段类型约束说明：

- 引用其他表字段命名规则，均为：`表名_ID`
- 数据字典字段的数据类型均为：`VARCHAR(4)`
- 是否的字段类型均为：`CHAR(1)`

### 8.2 表命名规范

**【强制】** 表名以数据类型前缀开头，前缀与表名之间用下划线分隔。

| 数据类型 | 数据类型（英文） | 前缀 | 数据库表样例 | 样例说明 |
| --- | --- | --- | --- | --- |
| 主数据 | Master Data Table | `TM` | `TM_BAS_PART` | 物料主数据 |
| 业务事务处理 | Transaction Data Table | `TT` | `TT_INVENTORY_DOCK_RECEIVE` | 道口收货记录 |
| 关系表 | Relationship Table | `TR` | `TR_SYS_USER_ROLE` | 角色与用户的关系 |
| 代码列表 | Code List Table | `TC` | `TC_SYS_CODE_TYPE` | 系统内数据库表元数据（数据字典） |
| 接口表 | Interface Table | `TI` | `TI_SAP_SUPPLIER` | 从 SAP 下载的供应商数据 |
| 系统管理表 | System administration Table | `TS` | `TS_SYS_PARAMETER` | 系统参数表 |
| 日志表 | Log Table | `TL` | `TL_SYS_USER_LOG` | 业务处理日志 |
| 历史表 | History Table | `TH` | `TH_BAS_PART` | 零件主数据的历史表 |
| 中间临时表 | Temparory table | `TE` | `TE_OFM_TEMPORDER` | 订单临时接收中间表 |
| 汇总表 | Aggregation Table | `TA` | — | 业务汇总报表 |
| 归档表 | Archivie Table | `TZ` | `TZ_BAS_PART` | 业务归档表，从历史数据到归档数据 |

### 8.3 主键、唯一键、外键等命名

**【强制】** 索引、主键、外键统一以"表名 + 后缀"命名。

| 数据类型 | 后缀 | 样例 |
| --- | --- | --- |
| 索引 | 表名 + `I` + 序号 | `TM_BAS_PART_I01` |
| 主键 | 表名 + `PK` | `TM_BAS_PART_PK` |
| 外键 | 表名 + `FK` + 序号 | `TM_BAS_PART_FK01` |

### 8.4 保留字段

**【强制】** 所有业务表统一包含以下保留字段。

| 字段名称 | 中文名称 | 类型 | 说明 |
| --- | --- | --- | --- |
| `ID` | 表 ID | `VARCHAR(36)` | |
| `CRT_USR_ID` | 创建人 ID | `VARCHAR(36)` | |
| `CRT_USR_NAME` | 创建人名称 | `VARCHAR(100)` | |
| `CRT_TIME` | 创建时间 | `DATETIME` | |
| `UPD_USR_ID` | 修改人 ID | `VARCHAR(36)` | |
| `UPD_USR_NAME` | 修改人名称 | `VARCHAR(100)` | |
| `UPD_TIME` | 修改时间 | `DATETIME` | |
| `TS_ENTER_ID` | 企业 ID | `VARCHAR(36)` | |
| `IS_DELETE` | 数据状态 | `CHAR(1)` | 0 有效，1 无效 |

### 8.5 字段与存储

**【强制】** 小数类型为 `decimal`，禁止使用 `float` 和 `double`。

> 说明：在存储的时候，`float` 和 `double` 都存在精度损失的问题，很可能在比较值的时候，得到不正确的结果。如果存储的数据范围超过 `decimal` 的范围，建议将数据拆成整数和小数并分开存储。

**【强制】** 如果存储的字符串长度几乎相等，使用 `char` 定长字符串类型。

**【强制】** `varchar` 是可变长字符串，不预先分配存储空间，长度不要超过 5000；如果存储长度大于此值，定义字段类型为 `text`，独立出来一张表，用主键来对应，避免影响其它字段索引效率。

**【强制】** 所有业务表数据不能做物理删除，全部做业务逻辑删除，根据 `is_delete` 状态进行区别。

**【推荐】** 在保证数据一致性的前提下，适当做数据冗余，以提高查询性能。规则如下：

1. 不是频繁修改的字段。
2. 不是唯一索引的字段。
3. 不是 `varchar` 超长字段，更不能是 `text` 字段。

**【推荐】** 单表行数超过 500 万行或者单表容量超过 2GB，才推荐进行分库分表。

> 说明：如果预计三年后的数据量根本达不到这个级别，请不要在创建表时就分库分表。

**【推荐】** `in` 操作能避免则避免，若实在避免不了，需要仔细评估 `in` 后边的集合元素数量，控制在 1000 个之内。

**【强制】** 在表查询中，一律不要使用 `*` 作为查询的字段列表，需要哪些字段必须明确写明。

> 说明：
> 1. 增加查询分析器解析成本。
> 2. 增减字段容易与 `resultMap` 配置不一致。
> 3. 无用字段增加网络消耗，尤其是 `text` 类型的字段。

## 九、经验积累（优化方向）

**【强制】** 数量统计查询禁止出现 List 查询后再取 `size` 大小，可直接用 `count` 查询。

正例：

![正例：直接使用 count 统计数量](assets/image6.png)

```xml
<select id="duplicateCheck" resultType="int">
    select count(*)
    from tt_stockinit
    where
    CODE = #{data.id,jdbcType=VARCHAR}
    and TS_ENTER_ID = #{data.tsEnterId,jdbcType=VARCHAR}
</select>
```

反例：

![反例：先查 List 再判断 size](assets/image7.png)

```java
List<Stock> list = this.stockMapper.custom(blur, BaseContextUtil.getEnterId(), query, sort);
if (list.size() > 0) {

}
```

**【强制】** 多表联合查询随着数据量增加会严重影响查询效率，初始查询限制在 5 张表以内（阿里要求 3 张表以内），其余关联逻辑在 Java 代码中做业务逻辑处理。

![反例：单条 SQL 联表过多](assets/image8.png)

```xml
<sql id="BASE_PUBLIC_SQL">
    select tbw.*,
    te.EQUIPMENTTYPE_CODE, te.EQUIPMENTTYPE_NAME,
    tbr.TM_BOM_ID, tbr.POSITION,
    tb.BOM_VERSION,
    tml.MATERIAL_CODE as productCode,
    tml.MATERIAL_NAME as productName,
    tml.STAGE_EDITION,
    tml.SPEC, tml.MODEL,
    tpc.PROC_CODE, tpc.PROC_NAME
    from tm_bom_wages tbw
    left join tm_equipmenttype te on tbw.TM_EQUIPMENTTYPE_ID = te.ID
    left join tm_material tml on tbw.TM_PRODUCT_ID = tml.ID
    left join tm_proc tpc on tbw.TM_PROC_ID = tpc.ID
    left join tm_bom_route tbr on tbw.TM_BOM_ROUTE_ID = tbr.ID
    left join tm_bom tb on tbr.TM_BOM_ID = tb.ID
</sql>
```

**【强制】** 所有外传参数首先判断非空，避免出现全表查询等现象。

正例：

![正例：入参为空时直接返回，避免全表查询](assets/image9.png)

```java
@Transactional(rollbackFor = Exception.class)
public WorkorderProductionReportData workorderSaveParams(List<TtWorkorderParameter> list) {
    WorkorderProductionReportData reportData = new WorkorderProductionReportData();
    if (CollectionUtil.isEmpty(list)) {
        return reportData;
    }
    // ...
}
```

**【强制】** 无用循环：业务只需 top(n) 时，无需查询大于 n 的数据，可直接采取分页查询对应的 n，`limit 0, n`。

![正例：内存分组后在 Java 侧排序取前 5 条，避免多余数据往返](assets/image10.png)

```java
@Override
public List<DigitalSignage> getStockData(JSONObject object) {
    Stock stock = new Stock();
    stock.setTsEnterId(BaseContextUtil.getEnterId());
    List<Stock> stocks = stockMapper.findStockList(stock);
    List<DigitalSignage> list = new ArrayList<>();
    Map<String, List<Stock>> map = stocks.stream().collect(Collectors.groupingBy(Stock::getTmWarehouseId));

    for (Map.Entry<String, List<Stock>> entry : map.entrySet()) {
        String k = entry.getKey();
        List<Stock> v = entry.getValue();
        DigitalSignage digitalSignage = new DigitalSignage();
        BigDecimal materialQty = v.stream().map(Stock::getAvailableStock).reduce(BigDecimal.ZERO, BigDecimal::add);
        digitalSignage.setMaterialQty(materialQty);
        digitalSignage.setWarehouseCode(k);
        digitalSignage.setWarehouseName(v.get(0).getWarehouseName());
        list.add(digitalSignage);
    }
    list = list.stream().sorted(Comparator.comparing(DigitalSignage::getMaterialQty, Comparator.reverseOrder())).limit(5).collect(Collectors.toList());
    return list;
}
```

**【强制】** `for` 循环中禁止出现数据库查询。

可以用 `in` 查询，MySQL 5.5 之后 `in` 查询索引生效。

反例：

![反例：循环体内逐条查询数据库](assets/image11.png)

```java
for (ProductionplanOut planOut : plantOuts) {
    Map<String, Object> planOutMap = new HashMap<>();
    DigitalSignage digitalSignage = new DigitalSignage();
    // ... 属性赋值
    planOutMap.put("order", digitalSignage);
    List<TtWorkorder> orders = ttWorkorderMapper.selectByTtProductionplanOutId(planOut.getId()); // 循环内查询
}
```

正例：可先 `for` 循环取出 `id` 等查询条件，利用 `in` 进行查询，再进行 `for` 双循环。

**【强制】** `for` 循环中禁止出现保存操作，这样会降低保存效率。

![反例：循环体内逐条 save](assets/image12.png)

```java
if (CollectionUtil.isNotEmpty(data.getChildren())) {
    // 保存生产计划-计划产出物料
    for (ProductionplanData.ProductionplanOutData outData : data.getChildren()) {
        outData.setTtProductionplanId(plan.getId());
        outData.setTmPlantId(plan.getTmPlantId());
        productionplanOutService.save(outData); // 循环内保存
    }
}
```

**【强制】** 开启事务的方法内，禁止出现 `try`、`catch`，可直接将异常抛出。

反例：

![反例：事务方法内 try-catch 吞掉异常](assets/image13.png)

```java
@Override
@Transactional
public int insert(T record) {
    try {
        try {
            if (StringUtils.isEmpty(((BaseEntity) record).getId())) {
                ((BaseEntity) record).setId(null);
            }
            ((BaseEntity) record).setCrtTime(new Date());
            ((BaseEntity) record).setCrtUsrId(BaseContextUtil.getUserId());
            ((BaseEntity) record).setCrtUsrName(BaseContextUtil.getName());
            ((BaseEntity) record).setUpdTime(new Date());
            ((BaseEntity) record).setUpdUsrId(BaseContextUtil.getUserId());
            ((BaseEntity) record).setUpdUsrName(BaseContextUtil.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
            // do nothing. keep go on..
        }
        return this.baseMapper.insertSelective(record);
    } catch (Exception ex) {
        log.debug("Error in AbstractBaseService.insert().");
        ex.printStackTrace();
        throw new RuntimeException(ex.getMessage());
    }
}
```

正例：

![正例：事务方法直接抛出异常，由上层统一处理](assets/image14.png)

```java
@Transactional(rollbackFor = Exception.class)
public WorkorderProductionReportData workorderSaveParams(List<TtWorkorderParameter> list) throws Exception {
    WorkorderProductionReportData reportData = new WorkorderProductionReportData();
    if (CollectionUtil.isEmpty(list)) {
        return reportData;
    }
    // 保存过程参数信息
    ttWorkorderParameterService.save(list);
    TtWorkorderBarcode workorderBarcode = new TtWorkorderBarcode();
    workorderBarcode.setBarcode(list.get(0).getBarcode());
    workorderBarcode.setTtWorkorderOutputId(list.get(0).getTtWorkorderOutputId());
    return workorderCompleteParams(workorderBarcode);
}
```

**【推荐】** Service 类中禁止出现 `private` 方法，如需要可以在工具类中实现。

**【强制】** 禁止使用 `group_concat` 函数，`group_concat` 是系统慢的主要原因。
