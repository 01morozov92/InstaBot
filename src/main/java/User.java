import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Builder.Default
    List<Post> postList = new ArrayList<>();

    private String login;
    private String password;

    public void addPost(Post post){
        postList.add(post);
    }
}
