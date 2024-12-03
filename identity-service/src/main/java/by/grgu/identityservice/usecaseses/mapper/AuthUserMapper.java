package by.grgu.identityservice.usecaseses.mapper;

//import com.example.demo.events.AuthUserGotEvent;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface AuthUserMapper {
//    AuthUserGotEvent userToAuthUserGotEvent(User user);

//    User authUserGotEventToUser(AuthUserGotEvent authUserGotEvent);
}
