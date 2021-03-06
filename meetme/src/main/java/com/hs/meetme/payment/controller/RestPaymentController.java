package com.hs.meetme.payment.controller;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hs.meetme.coupleinfo.domain.CoupleInfoVO;
import com.hs.meetme.coupleinfo.service.CoupleInfoService;
import com.hs.meetme.notice.domain.NoticeVO;
import com.hs.meetme.notice.service.NoticeService;
import com.hs.meetme.payment.api.GetTokenAPI;
import com.hs.meetme.payment.api.RefundAPI;
import com.hs.meetme.payment.domain.PaymentVO;
import com.hs.meetme.payment.service.PaymentService;
import com.hs.meetme.useraccess.domain.AccountVO;


@RestController
public class RestPaymentController {
	@Autowired PaymentService payService;
	@Autowired CoupleInfoService coupleService;
	@Autowired NoticeService noticeService;
	//Account VO는 다 session update
	@PostMapping("/payment") //결제
	public CoupleInfoVO paymentInsert(PaymentVO vo, HttpServletRequest request) {
		HttpSession session = request.getSession();
		AccountVO accountVO = (AccountVO) session.getAttribute("userSession");
		payService.paymentInsert(vo); //결제정보 DB입력, 커플신청 받지 못하도록 TRIGGER 발동
		System.out.println("결제정보 :"+vo);
		CoupleInfoVO oc = new CoupleInfoVO(); //커플정보 들고오기
		String result="";
		int coupleId =vo.getCoupleId();
		if(coupleId !=0) {  //신규 커플로그 이용자인지 확인
			oc.setCoupleId(coupleId);
		oc =coupleService.read(oc);  //커플의 기본정보 다 들고오기
		
			if(oc.getCoupleStatus().equals("n")) { //기존커플로그 만료로 새롭게 시작
				oc.setSubTerm(vo.getSubTerm());	
				Date sysdate = new Date(); 	// 현재 날짜
				oc.setStartDate(sysdate);	//현재 날짜를 시작일로 업데이트
				oc.setCoupleStatus("y");		//커플의 상태 변경
				coupleService.coupleInfoUpdate(oc); // subTerm,startDate,coupleStatus(y)로 변경
				oc.setUserId(oc.getUserRequest()); //userId에 req를 넣느다.
				coupleService.userCoupleStatusUpdate(oc); //유저테이블의 coupleStatus 둘 다 y변경
				accountVO.setCoupleStatus(oc.getCoupleStatus());
				result="커플로그기간이 새롭게 갱신되었습니다."
						+ "감사합니다.";
				oc.setMessage(result);//커플테이블의 상태가 n일 때 기존커플테이블 갱신
				
			}else if(oc.getCoupleStatus().equals("y")){ //기존커플로그 기간연장하기
				Calendar cal = Calendar.getInstance();
				cal.setTime(oc.getStartDate());
				cal.add(Calendar.MONTH, vo.getSubTerm()); //연장 기간 더해서 만료기간 계산하기
				Date endDate= cal.getTime();
				oc.setSubTerm(vo.getSubTerm());	//요금제 최신화
				oc.setStartDate(endDate); //startDate를 endDate로 초기화
				coupleService.coupleInfoUpdate(oc); //커플테이블 최신화
				result="커플로그기간이 연장되었습니다."
						+ "감사합니다."; //기존 커플의 연장 성공
				oc.setMessage(result);
			}else {
				result="비정상적인 접근입니다.";
				oc.setMessage(result);
			}
			
		}else {									//신규 커플로그 시작
			oc.setUserRequest(vo.getUserId());
			oc.setSubTerm(vo.getSubTerm());
			coupleService.coupleInfoInsert(oc); 
			oc =coupleService.read(oc);
			accountVO.setCoupleId(String.valueOf(oc.getCoupleId()));
			accountVO.setCoupleStatus("w");
			oc.setCoupleStatus("w");
			result="커플로그가 시작되었습니다."
					+ "감사합니다.";
			oc.setMessage(result);
		}	//커플테이블에 대한 정보가 없을 때 신규테이블 생성
		
		return oc;
	}
	
	
	@PostMapping("/refund") //환불하기
	public String refund(PaymentVO vo, HttpServletRequest request) { //결제정보 merchantUid를 들고 옵니다
		GetTokenAPI getToken =new GetTokenAPI();
		RefundAPI refund = new RefundAPI();
		HttpSession session = request.getSession();
		AccountVO accountVO = (AccountVO) session.getAttribute("userSession");
		String json=getToken.getToken(); //토큰 받아오기 API 메소드 실행
		vo.setToken(json); //토큰받아오기
		String refundDb= refund.Refund(vo); //환불 API 실행 후 결과 저장
		// 환불할 때 필요한 정보는 merchantUid랑 tokken 이다
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
        try {
             obj = (JSONObject)parser.parse(refundDb); //파싱 1
             JSONObject refundParsing=(JSONObject) obj.get("response");
            vo.setEmail(refundParsing.get("buyer_email").toString());
            vo.setUserName(refundParsing.get("buyer_name").toString());
            vo.setCancelAmount(Integer.parseInt(refundParsing.get("cancel_amount").toString()) );
            vo.setMerchantUid(refundParsing.get("merchant_uid").toString());
            System.out.println("들고온 친구들="+vo);
            //vo객체 안에 바로 넣어서 DB로 넘기면 된다.
             //cancel_amount,buyer_name,buyer_email,merchant_uid
        } catch (ParseException e) {
             e.printStackTrace();
        }
        payService.insertRefundInfo(vo); //환불결과 DB에 저장 , 커플상태 비활성화하는 TRIGGER 발동
      //구독 중 커플환불 시(insert) 커플테이블 활성화 상태 n, 구독기간 0으로 update TRIGGER 실행됩니다.
        
        //커플 상태, 유저 커플상태 변경
        CoupleInfoVO cvo=new CoupleInfoVO();
        NoticeVO nvo =new NoticeVO();
        cvo.setUserId(vo.getUserId());
        cvo= coupleService.userCoupleStatusRead(cvo); 
        String message ="";
	        if(cvo.getCoupleStatus().equals("w")) { //결제를 하고 커플 매칭하지 않고 환불
	        	coupleService.deleteCoupleInfo(cvo); //커플 테이블을 삭제해줌
	        	cvo.setCoupleStatus("n"); //커플 상태를 w->n으로 다시 변경
	        	accountVO.setCoupleStatus("n");
	        	coupleService.userCoupleStatusUpdate(cvo);
	        	message="환불되었습니다! 이용해 주셔서 감사합니다.";
	        }else if(cvo.getCoupleStatus().equals("s")) { //결제를 하고 커플 매칭중 환불 
	        	 nvo.setUserSent(String.valueOf(cvo.getUserId()));
	        	noticeService.deleteNotice(nvo); //커플 신청한 정보까지 제거
	        	coupleService.deleteCoupleInfo(cvo); //커플 테이블을 삭제해줌
	        	cvo.setCoupleStatus("n"); //커플 상태를 w->n으로 다시 변경
	        	accountVO.setCoupleStatus("n");
	        	coupleService.userCoupleStatusUpdate(cvo);
	        	message="환불되었습니다! 이용해 주셔서 감사합니다.";
	        }else{  						//구독 중 커플에 대한 환불
	        	cvo.setCoupleStatus("e"); //커플상태 m(커플이지만 구독기간 끝남)으로 변경
	        	accountVO.setCoupleStatus("e"); //session 커플상태 최신화
	        	coupleService.userCoupleStatusUpdate(cvo);
	        	message="환불되었습니다! \n"
	        			+ "더 나은 서비스를 위해 노력하겠습니다.";
	        }
	        
		return message;
	}
	
}
