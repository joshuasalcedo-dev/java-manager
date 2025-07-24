package io.joshuasalcedo.fx;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;


public class Launcher extends Application {

    private ConfigurableApplicationContext applicationContext;
    private final Logger logger = LoggerFactory.getLogger(Launcher.class);

    @Override
    public void init() throws Exception {
        logger.debug("Init MonorepoManagerFxApplication");
        applicationContext = new SpringApplicationBuilder(SpringApp.class)
                .headless(false) // Important for JavaFX
                .run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        logger.debug("Starting MonorepoManagerFxApplication");
        applicationContext.publishEvent(new StageReadyEvent(stage));

    }

    @Override
    public void stop() throws Exception {
        logger.debug("Stopping");
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        applicationContext.close();
        Platform.exit();
    }
}

@SpringBootApplication
class SpringApp {
    private static final Logger logger = LoggerFactory.getLogger(SpringApp.class);
    public static void main(String[] args) {
        logger.debug("SpringApp started");
        Application.launch(Launcher.class, args);
    }

}


@Component
class StageInitializer implements ApplicationListener<StageReadyEvent> {
    private final static Logger logger = LoggerFactory.getLogger(StageInitializer.class);
    static final String ASSETS_DIR = "/assets/";

    static final String APP_ICON_PATH = Objects.requireNonNull(
            Launcher.class.getResource(ASSETS_DIR + "icons/app-icon.png")
    ).toExternalForm();

    static final String APP_PROPERTIES_PATH = "/application.properties";

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();
        // obtain application properties from pom.xml
        loadApplicationProperties();

        var scene = new Scene(createWelcomePane(), 800, 600);
        scene.getStylesheets().add(ASSETS_DIR + "index.css");

        stage.setScene(scene);
        stage.setTitle(System.getProperty("app.name"));
        stage.getIcons().add(new Image(APP_ICON_PATH));
        stage.setOnCloseRequest(t -> Platform.exit());
        stage.setMaxWidth(1280);
        stage.setMaxHeight(900);

        Platform.runLater(() -> {
            stage.show();
            stage.requestFocus();
        });
    }

    private Pane createWelcomePane() {
        var root = new VBox();
        root.getStyleClass().add("welcome");
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(
                new FontIcon(MaterialDesignS.SCHOOL),
                new Label(
                        "Hi, this is the AtlantaFX starter project. Check out the README for a quick start and happy coding.")
        );

        return root;
    }

    private void loadApplicationProperties() {
        try {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream(APP_PROPERTIES_PATH)),
                    UTF_8
            ));
            properties.forEach((key, value) -> System.setProperty(
                    String.valueOf(key),
                    String.valueOf(value)
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class StageReadyEvent extends ApplicationEvent {
    private final Logger logger = LoggerFactory.getLogger(StageReadyEvent.class);

    public StageReadyEvent(Stage source) {
        super(source);
        logger.debug("StageReadyEvent");
    }

    public Stage getStage(){
        logger.debug("getStage");
        return (Stage)getSource();
    }
}
