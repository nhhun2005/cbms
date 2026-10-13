package cbms_backend.user.service;

import cbms_backend.core.exception.InvalidUserCredentialsException;
import cbms_backend.core.exception.UserAlreadyExistsException;
import cbms_backend.core.exception.UserAlreadyInactiveException;
import cbms_backend.core.exception.UserNotExistsException;
import cbms_backend.core.util.ApiResponse;
import cbms_backend.user.dto.create_account.CreateAccountRequest;
import cbms_backend.user.dto.update_user.UpdateNameRequest;
import cbms_backend.user.entity.User;
import cbms_backend.user.entity.UserStatus;
import cbms_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImplement implements UserService{
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<Void> createAccount(CreateAccountRequest request){
        if(repository.existsByPhone(request.phone())){
            throw new UserAlreadyExistsException("Phone number already exists");
        }
        User user = new User();
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.WORKING);

        repository.save(user);

        return ApiResponse.success("Account created");

    }

    @Override
    public ApiResponse<Void> updateName(UUID id, UpdateNameRequest request) {
        User user = repository.findById(id).orElseThrow(()-> new UserNotExistsException("User does not exists"));

        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new InvalidUserCredentialsException("Wrong password");
        }
        user.setName(request.name());
        repository.save(user);
        return ApiResponse.success("Name changed");
    }

    @Override
    public ApiResponse<Void> disableUser(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotExistsException("User does not exist"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UserAlreadyInactiveException("User is already inactive");
        }

        user.setStatus(UserStatus.INACTIVE);
        repository.save(user);

        return ApiResponse.success("User disabled successfully");
    }
}
