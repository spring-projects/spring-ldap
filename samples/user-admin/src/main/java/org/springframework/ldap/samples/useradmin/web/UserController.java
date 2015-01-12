/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.samples.useradmin.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.samples.useradmin.domain.DepartmentRepo;
import org.springframework.ldap.samples.useradmin.domain.User;
import org.springframework.ldap.samples.useradmin.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Mattias Hellborg Arthursson
 */
@Controller
public class UserController {

    private final AtomicInteger nextEmployeeNumber = new AtomicInteger(10);

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentRepo departmentRepo;

    @RequestMapping(value = {"/", "/users"}, method = GET)
    public String index(ModelMap map, @RequestParam(required = false) String name) {
        if(StringUtils.hasText(name)) {
            map.put("users", userService.searchByNameName(name));
        } else {
            map.put("users", userService.findAll());
        }
        return "listUsers";
    }

    @RequestMapping(value = "/users/{userid}", method = GET)
    public String getUser(@PathVariable String userid, ModelMap map) throws JsonProcessingException {
        
        map.put("isNew", false);
        map.put("user", userService.findUser(userid));
        populateDepartments(map);
        return "editUser";
    }

    @RequestMapping(value = "/newuser", method = GET)
    public String initNewUser(ModelMap map) throws JsonProcessingException {
        User user = new User();
        user.setEmployeeNumber(nextEmployeeNumber.getAndIncrement());

        map.put("isNew", true);
        map.put("user", user);
        populateDepartments(map);

        return "editUser";
    }

    private void populateDepartments(ModelMap map) throws JsonProcessingException {
        Map<String, List<String>> departmentMap = departmentRepo.getDepartmentMap();
        ObjectMapper objectMapper = new ObjectMapper();
        String departmentsAsJson = objectMapper.writeValueAsString(departmentMap);
        map.put("departments", departmentsAsJson);
    }

    @RequestMapping(value = "/newuser", method = POST)
    public String createUser(User user) {
        User savedUser = userService.createUser(user);

        return "redirect:/users/" + savedUser.getId();
    }

    @RequestMapping(value = "/users/{userid}", method = POST)
    public String updateUser(@PathVariable String userid, User user) {
        User savedUser = userService.updateUser(userid, user);

        return "redirect:/users/" + savedUser.getId();
    }
}
