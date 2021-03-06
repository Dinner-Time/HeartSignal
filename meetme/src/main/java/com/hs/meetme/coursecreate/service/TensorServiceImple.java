package com.hs.meetme.coursecreate.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hs.meetme.coursecreate.domain.TagNumAddrYearVO;
import com.hs.meetme.coursecreate.domain.TensorCauseVO;
import com.hs.meetme.coursecreate.domain.TensorResultVO;
import com.hs.meetme.coursecreate.domain.TensorVO;
import com.hs.meetme.coursecreate.mapper.TensorMapper;

@Service
public class TensorServiceImple implements TensorService {

	@Autowired TensorMapper tensorMapper;
	
	@Override
	public List<TensorVO> createTensorData() {
		return tensorMapper.createTensorData();
	}

	@Override
	public List<TensorCauseVO> createTensorCauseData() {
		return tensorMapper.createTensorCauseData();
	}

	@Override
	public List<TensorResultVO> createTensorResultData() {
		return tensorMapper.createTensorResultData();
	}

	@Override
	public List<TagNumAddrYearVO> getDataOfTensor(String userId) {
		return tensorMapper.getDataOfTensor(userId);
	}

}
