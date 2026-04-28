package com.lk.aizerocodeplatform.generate;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;
import java.util.Objects;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/21 21:23
 */
public class MybatisFlexCodeGenerate {
    public static void main(String[] args) {
        // 获取数据源信息
        Dict dict = YamlUtil.loadByPath("application-local.yaml");
        Map<String, Objects> datasourceConfig = dict.getByPath("spring.datasource");
        String url = String.valueOf(datasourceConfig.get("url"));
        String username = String.valueOf(datasourceConfig.get("username"));
        String password = String.valueOf(datasourceConfig.get("password"));
        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfigUseStyle2();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfigUseStyle2() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        globalConfig.getPackageConfig()
                .setBasePackage("com.lk.aizerocodeplatform.generate_result");

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
//                .setTablePrefix("tb_")
//                .setGenerateTable("user");
                .setGenerateTable("chat_history");
        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(17);

        //设置生成 mapper
        globalConfig.enableMapper();

        //设置生成mapper.xml文件
        globalConfig.enableMapperXml();

        //设置生成的 service
        globalConfig.enableService();

        // 设置生成的serviceImpl
        globalConfig.enableServiceImpl();

        // 设置生成controller
        globalConfig.enableController();


        return globalConfig;
    }
}
