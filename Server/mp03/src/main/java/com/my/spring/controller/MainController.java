package com.my.spring.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.my.spring.domain.RelationVO;
import com.my.spring.domain.UserVO;
import com.my.spring.domain.WorkerViewVO;
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
	@RequestMapping("/all")
	public List<UserVO> viewAll() {
		return userService.viewAll();
	}
	
	// ���� ��ȸ
	@RequestMapping(value = "/account/{account}", method = RequestMethod.GET)
	public UserVO searchByAccount(@PathVariable String account) {
		if (userService.searchByAccount(account) != null) {
			return userService.searchByAccount(account);
		} else {
			UserVO allowed = new UserVO();
			allowed.setAccount("allowed");
			return allowed;
		}
	}
	
	// ���� ����
	@RequestMapping(value = "", method = RequestMethod.POST)
	public Boolean createAccount(@RequestBody UserVO newUser) {

		System.out.println(newUser.getId());
		System.out.println(newUser.getRole());
		System.out.println(newUser.getAccount());
		System.out.println(newUser.getNick());
		System.out.println(newUser.getPw());
		System.out.println(newUser.getName());
		System.out.println(newUser.getAge());
		return userService.createAccount(newUser);
	}
	
	// ID <-> PW ��Ī ����: True -> ��ġ False -> ����ġ
	@RequestMapping(value = "/login/{account}/{pw}", method = RequestMethod.GET)
	public Boolean checkValidation(@PathVariable String account, @PathVariable String pw) {
		if (userService.searchByAccount(account) != null) {
			return userService.searchByAccount(account).getPw().equals(pw);
		} else {
			return false;
		}
	}
	
	// WorkerView ������Ʈ ��ȯ
	@RequestMapping(value="/worker/{id_branch}", method = RequestMethod.GET)
	public List<WorkerViewVO> getWorkerViewByIdBranch(@PathVariable int id_branch) {
		System.out.println();
		System.out.println("::getWorkerViewByIdBranch::");
		System.out.println("id_branch\t|\t" + id_branch);
		System.out.println();
		List<WorkerViewVO> result = userService.getWorkerViewByIdBranch(id_branch);
		for (WorkerViewVO one : result) {
			System.out.println(one.getWage());
		}
		return userService.getWorkerViewByIdBranch(id_branch);
	}
	
	@RequestMapping(value="/relation", method = RequestMethod.POST)
	public Boolean createRelation(@RequestBody RelationVO newRelation) {
		System.out.println();
		System.out.println("::createRelation::");
		System.out.println("id_branch: " + newRelation.getId_branch());
		System.out.println("id_worker: " + newRelation.getId_worker());
		return userService.createRelation(newRelation);
	}
	
	@RequestMapping(value="/id_worker/{id_worker}")
	public String getAccountById(@PathVariable int id_worker) {
		return userService.getAccountById(id_worker);
	}
}
