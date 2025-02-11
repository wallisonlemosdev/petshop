package com.wallisonlemos.petshop.mock;

import com.wallisonlemos.petshop.model.dto.auth.LoginDto;
import com.wallisonlemos.petshop.utils.test.JsonTestUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Component
public class TokenMock {

    @Autowired
    private MockMvc mockMvc;

    public String getToken() throws Exception {
        LoginDto authDto = new LoginDto("06777953000", "Pet0123456");
        JsonTestUtil<Object> jsonTestAuth = new JsonTestUtil<>();

        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestAuth.parseToJSONString(authDto)))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        JSONObject jsonResponse = new JSONObject(responseJson);
        return jsonResponse.getString("token");
    }
}
