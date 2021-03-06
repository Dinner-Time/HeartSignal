package com.hs.meetme.post.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.hs.meetme.mypage.domain.CommentVO;
import com.hs.meetme.mypage.domain.Criteria;
import com.hs.meetme.mypage.domain.MyPageCourseVO;
import com.hs.meetme.mypage.domain.PageVO;
import com.hs.meetme.mypage.domain.PostVO;
import com.hs.meetme.notice.domain.NoticeVO;
import com.hs.meetme.notice.service.NoticeService;
import com.hs.meetme.post.service.PostService;
import com.hs.meetme.useraccess.domain.AccountVO;

import lombok.extern.java.Log;

@Controller
@RequestMapping("/post")
@Log
public class PostController {

	@Autowired
	PostService pService;
	@Autowired
	NoticeService noticeService;

	File fileDir = new File("src/main/resources/static/img/");

	// ???????????? ????????? ??????... ????????? ?????????!
	@GetMapping("/community_list")
	public String community_list(Model model, Criteria cri) {
//		cri.setAmount(20);
		int total = pService.getTotalCmNum(cri);
//		System.out.println("?????? ==== "+total);
		model.addAttribute("count", pService.getCmNum());

		List<PostVO> list = new ArrayList<>();
		list = pService.getCMList(cri);
//		System.out.println("????????? ????????????"+list);
		model.addAttribute("list", list);
		model.addAttribute("pageMaker", new PageVO(cri, total));
		return "post/community_list";
	}

	// ???????????? ??? ????????????
	@GetMapping("/get_community/{postId}")
	public String get_community(@PathVariable long postId, Model model, HttpServletRequest request) {
		HttpSession session = request.getSession();
		AccountVO accountVO = (AccountVO) session.getAttribute("userSession");
		
		if(accountVO == null) {
			return "redirect:/login";
		}
		
		String userId = accountVO.getUserId();

		PostVO post = pService.getPost(postId);
		post.setUserId(userId);
				
		// ????????? ????????? 1 ???????????? 0
		int isLike = pService.getPostLike(post);
		model.addAttribute("like", isLike);
		
		// ????????? ????????? 1 ???????????? 0
		int isScraped = pService.getCourseScrap(post);
		model.addAttribute("scrap", isScraped);
		
		String courseId = post.getCourseId();
		model.addAttribute("list", pService.getPost(postId));
		model.addAttribute("cmt", pService.commentCM(postId));
		int count = pService.countCM(postId);
		model.addAttribute("count",count);
		if (courseId != null) {
			model.addAttribute("course", pService.getCourse(courseId));
		}

		pService.countHit(postId);

		return "post/community_get";
		/*
		 * if(idx==null) { //???????????? ?????? ?????? return "post/community_lis"; }
		 * 
		 * PostVO post = pService.getPost(); if(board==null ||
		 * "Y".equals(board.getDeleteYn())) { //?????? ????????? or ?????? ????????? ????????? return
		 * "redirect:/board/list.do"; } model.addAttribute("board",board);
		 * 
		 * return "board/view";
		 */
	}
	
	

	// ???????????? ??? ??????
	@GetMapping("/community_insert")
	public String community_insert(Model model,HttpServletRequest request, MyPageCourseVO vo) {
		HttpSession session = request.getSession();
		AccountVO accountVO = (AccountVO) session.getAttribute("userSession");
		vo.setUserId(accountVO.getUserId());
		model.addAttribute("places", pService.getPlaceList(vo));
		model.addAttribute("list", pService.getCourseList(vo));

		return "post/community_insert";
	}

	// ???????????? ????????? ??????
	@GetMapping("/insertCommunity")
	public String insertCommunity(@ModelAttribute PostVO vo, HttpServletResponse insert/* , HttpSession session */) {
		pService.CMInsert(vo);
		return "redirect:/post/community_list";
	}

	// ???????????? ?????? ??? ?????????
	@GetMapping("/updateCommunity")
	public String updateCommunity(Model model, PostVO vo,  MyPageCourseVO pVo) {
		PostVO list = pService.getPost(Long.parseLong(vo.getPostId()));
		model.addAttribute("list", list);
		String courseId = list.getCourseId();
		
		if (courseId != null) {
			model.addAttribute("course", pService.getCourse(courseId));
		}
		String userId = list.getUserId();
		pVo.setUserId(userId);
		model.addAttribute("cList", pService.getCourseList(pVo));
		model.addAttribute("places", pService.getPlaceList(pVo));
		
		return "post/community_update";
	}

	// ???????????? ??????
	@PostMapping("/updateCommunity")
	public String update_community(@ModelAttribute PostVO vo, HttpServletResponse update/* , HttpSession session */) {
		System.out.println("vo =="+vo);
		pService.postUpdate(vo);
		int postId = Integer.parseInt(vo.getPostId());
		System.out.println("postId == "+postId);
		String page = "redirect:/post/get_community/"+postId;
		return page;
	}

	// ????????????, ?????? ?????? ??????????????????.. redirect?????????????!
	@PostMapping("/postDelete")
	public String postDelete(@ModelAttribute PostVO vo) {
		pService.postDelete(vo);
		System.out.println(vo);
		return "redirect:/post/community_list";
	}

	/*
	 * //???????????? ?????? ????????????
	 * 
	 * @GetMapping("/commentCM") public String commentCM(CommentVO cVo) {
	 * pService.commentCM(Long.parseLong(cVo.getPostId())); return
	 * "post/community_get"; }
	 */

	// ???????????? ?????? ??????
	@ResponseBody
	@PostMapping("/insertCMComment")
	public CommentVO insertCMComment(@RequestBody CommentVO vo, HttpServletRequest request) {
		
		
		HttpSession session = request.getSession();
		pService.insertCMComment(vo);
		AccountVO accountVO = (AccountVO) session.getAttribute("userSession");
		vo.setNickname(accountVO.getNickname());
		vo.setImgUrl(accountVO.getImgUrl());
		
		//notice INSERT 
		System.out.println("????????????"+vo.getUserId()+"+"+"??????????????? "+vo.getPostUserId());
		if(!vo.getUserId().equals(vo.getPostUserId())) { 		//????????? ???????????? ?????????
			NoticeVO nVo = new NoticeVO();
			nVo.setUserSent(vo.getUserId()); 			//?????? ??? ??????
			nVo.setUserReceived(vo.getPostUserId()); 	//????????? ??? ??????
			nVo.setPostId(vo.getPostId()); 				// ????????? ?????? ??????
			nVo.setNoticeContent("\""+vo.getPostTitle()+"\"??? "+accountVO.getNickname()+"?????? ????????? ???????????????.");
			noticeService.insertNotice(nVo);
			System.out.println("???????????? ???????????? INSERT"+nVo);
		}
		//notice INSERT end

		return vo;
	}

	//???????????? ?????? ??????
	@ResponseBody
	@PutMapping("/commentUpdate")
	public CommentVO commentUpdate(@RequestBody CommentVO vo) {
		pService.commentUpdate(vo);
		return vo;
	}

	//???????????? ?????? ??????
	@ResponseBody
	@DeleteMapping("/delCMComment/{commentId}")
	public int delCMComment(@PathVariable long commentId) {
		return pService.commentDelete(commentId);
	}

	//??? ?????????(?????????)
	@ResponseBody
	@PostMapping("/postLike")
	public int postLike(@RequestBody Map<String, Long> map) {
		long postId = map.get("postId");
		long userId = map.get("userId");
		return pService.postLike(postId, userId);
	}
	
	
	//?????? ?????????
	@ResponseBody
	@PostMapping("/scrapCourse")
	public int scrapCourse(@RequestBody Map<String, Long> map) {
		long courseId = map.get("courseId");
		long userId = map.get("userId");
		return pService.scrapCourse(courseId, userId);
	}
	

	//??? ????????? ??????
	@ResponseBody
	@DeleteMapping("/postLikeCancel")
	public int postLikeCancel(@RequestBody Map<String, Long> map) {
		long postId = map.get("postId");
		long userId = map.get("userId");
		return pService.postLikeCancle(postId, userId);
	}
	
	//?????? ????????? ??????
	@ResponseBody
	@DeleteMapping("/scrapCourseCancel")
	public int scrapCourseCancel(@RequestBody Map<String, Long> map) {
		long courseId = map.get("courseId");
		long userId = map.get("userId");
		return pService.scrapCancel(courseId, userId);
	}

	
	@RequestMapping(value = "/ckeditor/fileUpload", method = RequestMethod.POST)
	public void imageUpload(HttpServletRequest request, HttpServletResponse response,
			MultipartHttpServletRequest multiFile, @RequestParam MultipartFile upload) throws Exception {
		// ?????? ?????? ??????
		UUID uid = UUID.randomUUID();
		OutputStream out = null;
		PrintWriter printWriter = null;
		// ?????????
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		try {
			// ?????? ?????? ????????????
			String fileName = upload.getOriginalFilename();
			byte[] bytes = upload.getBytes();

			
			// ????????? ?????? ??????
//			String path = fileDir.getAbsolutePath() + "/ckImage/";
			// fileDir??? ?????? ????????? ?????? ????????? ?????? ??????????????? ??????.
			
			String path = request.getSession().getServletContext().getRealPath("/img/ckImage");
			String ckUploadPath = path + uid + "_" + fileName;
			File folder = new File(path);
			// ?????? ???????????? ??????
			if (!folder.exists()) {
				try {
					folder.mkdirs();
					// ?????? ??????
				} catch (Exception e) {
					e.getStackTrace();
				}
			}
			out = new FileOutputStream(new File(ckUploadPath));
			out.write(bytes);
			out.flush();
			// outputStram??? ????????? ???????????? ???????????? ?????????
			String callback = request.getParameter("CKEditorFuncNum");
			printWriter = response.getWriter();
			String fileUrl = "/post/ckImgSubmit?uid=" + uid + "&fileName=" + fileName;
			// ????????????
			// ???????????? ????????? ??????
			printWriter.println("{\"filename\" : \"" + fileName + "\", \"uploaded\" : 1, \"url\":\"" + fileUrl + "\"}");
			printWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (printWriter != null) {
					printWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;

	}

	@RequestMapping(value = "/ckImgSubmit")
	public void ckSubmit(@RequestParam(value = "uid") String uid, @RequestParam(value = "fileName") String fileName,
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// ????????? ????????? ????????? ??????
//		String path = fileDir.getAbsolutePath() + "/ckImage/";
		String path = request.getSession().getServletContext().getRealPath("/img/ckImage");
		String sDirPath = path + uid + "_" + fileName;
		File imgFile = new File(sDirPath);
		// ?????? ????????? ?????? ????????? ?????? ??????????????? ??? ????????? ????????? ????????????.
		if (imgFile.isFile()) {
			byte[] buf = new byte[1024];
			int readByte = 0;
			int length = 0;
			byte[] imgBuf = null;
			FileInputStream fileInputStream = null;
			ByteArrayOutputStream outputStream = null;
			ServletOutputStream out = null;
			try {
				fileInputStream = new FileInputStream(imgFile);
				outputStream = new ByteArrayOutputStream();
				out = response.getOutputStream();
				while ((readByte = fileInputStream.read(buf)) != -1) {
					outputStream.write(buf, 0, readByte);
				}
				imgBuf = outputStream.toByteArray();
				length = imgBuf.length;
				out.write(imgBuf, 0, length);
				out.flush();
			} catch (IOException e) {
				log.info(e.toString());
			} finally {
				outputStream.close();
				fileInputStream.close();
				out.close();
			}
		}
	}
}
