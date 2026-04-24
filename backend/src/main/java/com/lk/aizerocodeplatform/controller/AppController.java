package com.lk.aizerocodeplatform.controller;

import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.lk.aizerocodeplatform.model.entity.App;
import com.lk.aizerocodeplatform.service.AppService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 控制层。
 *
 * @author LK
 * @since 2026-04-24
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;



}
