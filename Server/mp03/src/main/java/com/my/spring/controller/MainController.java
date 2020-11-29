package com.my.spring.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.my.spring.domain.UserVO;
import com.my.spring.service.UserService;

/**
 * Handles requests for the application home page.
 */
@RestController
@RequestMapping("/user")
public class MainController {
	
	@Autowired
	private UserService userService;
	
	// ��ü ��ȸ
	@RequestMapping("")
	public List<UserVO> viewAll() {
		return userService.viewAll();
	}
	
	// ���� ��ȸ
	@RequestMapping(value = "/account/{account}", method = RequestMethod.GET)
	public UserVO searchByAccount(@PathVariable String account) {
		return userService.searchByAccount(account);
	}
	
	// ���� ����
	@RequestMapping(value = "", method = RequestMethod.POST)
	public Boolean createAccount(@RequestBody UserVO newUser) {
		return userService.createAccount(newUser);
	}
	
	// ID <-> PW ��Ī ����: True -> ��ġ False -> ����ġ
	@RequestMapping(value = "/login/{account}/{pw}", method = RequestMethod.GET)
	public Boolean checkValidation(@PathVariable String account, @PathVariable String pw) {
		return userService.searchByAccount(account).getPw().equals(pw);
	}
}
