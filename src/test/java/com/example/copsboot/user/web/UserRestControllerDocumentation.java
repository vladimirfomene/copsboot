package com.example.copsboot.user.web;



import com.example.copsboot.infrastructure.test.CopsbootControllerTest;
import com.example.copsboot.user.UserService;
import com.example.copsboot.user.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import java.util.Optional;

import static com.example.copsboot.infrastructure.security.SecurityHelperForMockMvc.HEADER_AUTHORIZATION;
import static com.example.copsboot.infrastructure.security.SecurityHelperForMockMvc.bearer;
import static com.example.copsboot.infrastructure.security.SecurityHelperForMockMvc.obtainAccessToken;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@CopsbootControllerTest(UserRestController.class)
public class UserRestControllerDocumentation {

    @Rule
    public final JUnitRestDocumentation restDocumentation = new
            JUnitRestDocumentation("target/snippets");

    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService service;

    @Autowired
    private WebApplicationContext context;

    private RestDocumentationResultHandler resultHandler;

    @Before
    public void setUp() {
        resultHandler = document("{method-name}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint(),
                        removeMatchingHeaders("X.*",
                                "Pragma",
                                "Expires")));
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(resultHandler)
                .build();
    }

    @Test
    public void ownUserDetailsWhenNotLoggedInExample() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void authenticatedOfficerDetailsExample() throws Exception {
        String accessToken = obtainAccessToken(mvc, Users.OFFICER_EMAIL, Users
                .OFFICER_PASSWORD);
        when(service.getUser(Users.officer().getId())).thenReturn(Optional.of(Users
                .officer()));
        mvc.perform(get("/api/users/me")
                .header(HEADER_AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andDo(resultHandler.document(
                        PayloadDocumentation.responseFields(
                                PayloadDocumentation.fieldWithPath("id")
                                        .description("The unique id of the user."),
                                PayloadDocumentation.fieldWithPath("email")
                                        .description("The email address of the user."),
                                PayloadDocumentation.fieldWithPath("roles")
                                        .description("The security roles of the user."))));
    }


    @Test
    public void createOfficerExample() throws Exception {

        String email = "wim.deblauwe@example.com";
        String password = "my-super-secret-pwd";

        CreateOfficerParameters parameters = new CreateOfficerParameters();
        parameters.setEmail(email);
        parameters.setPassword(password);

        when(service.createOfficer(email, password)).thenReturn(Users.newOfficer(email,
                password));
        mvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(parameters)))
                .andExpect(status().isCreated())
                .andDo(resultHandler.document(
                        PayloadDocumentation.requestFields(
                                PayloadDocumentation.fieldWithPath("email").description("The email " +
                                        "address of the user to be created."),
                                                PayloadDocumentation.fieldWithPath("password")
                                                        .description("The password for the new user.")
                                        ),
                                PayloadDocumentation.responseFields(
                                        PayloadDocumentation.fieldWithPath("id")
                                                .description("The unique id of the user."),
                                        PayloadDocumentation.fieldWithPath("email")
                                                .description("The email address of the user."),
                                        PayloadDocumentation.fieldWithPath("roles")
                                                .description("The security roles of the user."))));
    }
}
