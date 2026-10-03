package cbms_backend.user.service;

import cbms_backend.core.exception.UserAlreadyExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.entity.User;
import cbms_backend.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImplement implements UserService{
    UserRepository repository;
    PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<Void> createAccount(CreateAccountRequest request){
        if(repository.existsByPhone(request.phone())){
            throw new UserAlreadyExistsException("Phone number already exists");
        }
        User user = new User();
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setPassword(passwordEncoder.encode(request.password()));

        repository.save(user);

        return ApiResponse.success("Account created");

    }


}
