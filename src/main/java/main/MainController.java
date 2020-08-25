package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller

public class MainController {

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    private String clientId = "101991324239-kk7trh5iqhhj9o451skhh8618q6v4dm3.apps.googleusercontent.com";
    private String secretId = "UOM-3Kubqp0EK22iCSIM2_nd";

    @GetMapping("/login")
    public String login (HttpServletRequest request){
        String url = "https://accounts.google.com/o/oauth2/v2/auth?"+
        "response_type=code&"+
                "client_id="+clientId+"&"+
                "scope=openid%20email%20profile&"+
                "redirect_uri=http%3A//localhost%3A8080/code&"+
                "state=security_token%3D138r5719ru3e1%26url%3Dhttps%3A%2F%2Foauth2-login-demo.example.com%2FmyHome&"+
                "login_hint=aaa@gamil.com"+
                "nonce=0394852-3190485-2490358&access_type=offline";
                //"hd=goni.com";

        return "redirect:"+url;
    }

    @GetMapping("/")
    public String main(){
        return "index";
    }

    @GetMapping("/code")
    public String codeReceive(HttpServletRequest request, Model model) throws IOException {
        String receiveSession = request.getParameter("state");
        String code = request.getParameter("code");
        System.out.println(code);

//        model.addAttribute("code", code);
//        model.addAttribute("client_id", clientId);
//        model.addAttribute("client_secret", secretId);
//        model.addAttribute("redirect_uri", "http://localhost:8080/code");
//        model.addAttribute("grant_type", "authorization_code");
//        return "requestToken.html";

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("code", code);
        parameters.add("client_id", clientId);
        parameters.add("client_secret", secretId);
        parameters.add("redirect_uri", "http://localhost:8080/code");
        parameters.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(parameters, headers);

        ResponseEntity<Map> responseEntity = restTemplate.exchange("https://www.googleapis.com/oauth2/v4/token", HttpMethod.POST, requestEntity, Map.class);
        Map<String, Object> responseMap = responseEntity.getBody();




        // id_token 라는 키에 사용자가 정보가 존재한다.
        // 받아온 결과는 JWT (Json Web Token) 형식으로 받아온다. 콤마 단위로 끊어서 첫 번째는 현 토큰에 대한 메타 정보, 두 번째는 우리가 필요한 내용이 존재한다.
        // 세번째 부분에는 위변조를 방지하기 위한 특정 알고리즘으로 암호화되어 사이닝에 사용한다.
        //Base 64로 인코딩 되어 있으므로 디코딩한다.

        String[] tokens = ((String)responseMap.get("id_token")).split("\\.");
        Base64 base64 = new Base64(true);
        String body = new String(base64.decode(tokens[1]));

        System.out.println(tokens.length);
        System.out.println(new String(Base64.decodeBase64(tokens[0]), "utf-8"));
        System.out.println(new String(Base64.decodeBase64(tokens[1]), "utf-8"));

        //Jackson을 사용한 JSON을 자바 Map 형식으로 변환
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> result = mapper.readValue(body, Map.class);
        System.out.println(result.get(""));

        model.addAttribute("jwtEmail", result.get("email"));

        return "/index";
    }


    @GetMapping("QRGen")
    public String GenerateKey(Model model){
        GoogleOTP otpGen = new GoogleOTP();
        HashMap<String , String> map = otpGen.generate("gnhyun", "hist.co.kr");
        model.addAttribute("QRUrl", map.get("url"));
        model.addAttribute("encodedKey", map.get("encodedKey"));

        return "TOTPTest";
    }

}
