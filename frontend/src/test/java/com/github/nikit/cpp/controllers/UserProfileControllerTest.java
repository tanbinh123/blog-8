package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.TestConstants;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserProfileControllerTest extends AbstractUtTestRunner {

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void testGetAliceProfileWhichNotContainsPassword() throws Exception {
        MvcResult getPostsRequest = mockMvc.perform(
                get(Constants.Uls.API+Constants.Uls.PROFILE)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("alice"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();
    }

    @Test
    @Ignore
    public void fullyAuthenticatedUserCanChangeHisProfile() {

        // assert that password isn't affected
    }

    @Test
    @Ignore
    public void fullyAuthenticatedUserCannotChangeForeignProfile() {

    }

    @Test
    @Ignore
    public void adminCanChangeAnyProfile() {

    }

    @Test
    @Ignore
    public void adminCanSeeAnybodyProfileEmail() {

    }

    @Test
    @Ignore
    public void userCannotSeeAnybodyProfileEmail() {

    }
    @Test
    @Ignore
    public void userCanSeeOnlyOwnProfileEmail() {

    }

}
