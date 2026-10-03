package cbms_backend.core.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
@AllArgsConstructor
public class ApiResponse<T> {
    private T data;
    private String message;

    public static <T> ApiResponse<T> success(){
        return new ApiResponse<T>(null, "Success");
    }
    public static <T> ApiResponse<T> success(String message){
        return new ApiResponse<T>(null, message);
    }

    public static <T> ApiResponse<T> error(){
        return new ApiResponse<T>(null, "Request is invalid");
    }
    public static <T> ApiResponse<T> error(String message){
        return new ApiResponse<T>(null, message);
    }

}
