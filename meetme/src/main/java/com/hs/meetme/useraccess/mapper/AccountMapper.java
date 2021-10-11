package com.hs.meetme.useraccess.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.hs.meetme.useraccess.domain.AccountVO;

@Mapper
public interface AccountMapper {
	public AccountVO findByEmail(String email);
	public int emailCheck(String email);
	public int nicknameCheck(String nickname);
	public void signUp(AccountVO vo);
}
