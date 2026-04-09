package com.example.openglexample.unitTestDemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(User.class)
public class UserServiceTest {
    @Test
    public void testFindUserById() {
        // Arrange
        UserRepository mockRepository = mock(UserRepository.class);
        when(mockRepository.findById("123")).thenThrow(new RuntimeException());
        UserService userService = new UserService(mockRepository);

        // Act
        try {
            userService.findUserById("123");
            fail("Expected an RuntimeException to be thrown");
        } catch (RuntimeException e) {
            // Assert
            assertEquals("java.lang.RuntimeException", e.toString());
        }
    }

    @Test
    public void testFindUserByIdThroughConstruct() throws Exception {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("345");
        whenNew(User.class).withArguments("123").thenReturn(mockUser);

        // Act
        User user = new User("123");

        // Assert
        assertNotEquals("123", user.getId());
    }
}
