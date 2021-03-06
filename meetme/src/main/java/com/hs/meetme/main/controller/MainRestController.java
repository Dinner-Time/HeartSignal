package com.hs.meetme.main.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hs.meetme.main.domain.MainVO;
import com.hs.meetme.main.mapper.MainMapper;
import com.hs.meetme.main.service.MainService;

@RestController
@RequestMapping("/search/*")
public class MainRestController {
	@Autowired
	MainMapper mapper;
	@Autowired
	MainService service;
	
	@GetMapping("/{address}")
	public List<MainVO> addressSec(@PathVariable String address) {
		return service.addressSec(address);
	}

	@GetMapping("/{address}/{detailAddr}")
	public List<MainVO> courseList(@PathVariable String address, @PathVariable String detailAddr) {
		String fullAddr = address + " " + detailAddr;
		return service.courseList(fullAddr);
		
	}
	
	@GetMapping("/")
	public List<MainVO> placeList(String[] list) {
		
		List<MainVO> sendList = null;
	  for(String item : list) {
		  System.out.println(item);
		  int placeId = Integer.parseInt(item);
		  List<MainVO> sendListIn = service.placeList(placeId);
		  for(MainVO vo : sendListIn) {
			  sendList.add(vo);
		  }
		  
	  }
		 
	  return sendList;
	}
	

}

