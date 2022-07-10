package fogcomputing.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClientProperties implements Serializable {

    private String remoteHost = "localhost";
    private String volcanoName = "example volcano";
    // private UUID uuidSensor: // ?


}
