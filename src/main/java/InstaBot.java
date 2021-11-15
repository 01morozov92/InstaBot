import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.updateshandlers.DownloadFileCallback;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class InstaBot extends TelegramLongPollingBot {
    Map<Long, User> users = new HashMap<>();
    private final String RESOURCE_PATH = new java.io.File("src/test/resources").getAbsolutePath();

    @Override
    public String getBotUsername() {
        return "@insta_juma_bot";
    }

    @SneakyThrows
    @Override
    public String getBotToken() {
        Properties properties = new Properties();
        properties.load(new FileInputStream("application.properties"));
        return properties.getProperty("token");
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        if (!users.containsKey(userId)) {
            execute(SendMessage.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .text("Пришлите пожалуйста ваши: логин и пароль в одном сообщении через пробел, чтобы я мог опубликовать ваш пост в Instagram")
                    .build());
            users.put(userId, null);
        } else {
            if (users.get(userId) == null) {
                String[] credentials = update.getMessage().getText().split(" ");
                User user = User.builder()
                        .login(credentials[0])
                        .password(credentials[1])
                        .build();
                users.put(userId, user);
                sendMessage(update, "Отлично, теперь можете прислать фото и текст в одном сообщении");
            } else if (update.getMessage().getPhoto().size() > 0) {
                Post post = new Post();
                post.setTitle(update.getMessage().getText());
                File file = execute(GetFile.builder()
                        .fileId(update.getMessage().getPhoto().get(0).getFileId())
                        .build());
                String fullPath = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();
                System.out.println(fullPath);
                String fullPhotoPath = RESOURCE_PATH + "/" + update.getMessage().getPhoto().get(0).getFileId() + ".jpg";
                try {
                    HttpDownload.downloadFile(fullPath, RESOURCE_PATH, update.getMessage().getPhoto().get(0).getFileId() + ".jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                post.setPhoto(new java.io.File(fullPhotoPath).getCanonicalPath());
                users.get(userId).addPost(post);
                System.out.println(users.toString());
            }
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        InstaBot bot = new InstaBot();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }

    @SneakyThrows
    public void sendMessage(Update update, String text) {
        execute(SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text(text)
                .build());
    }
}
