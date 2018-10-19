package com.maxbill.core.desktop;

import com.maxbill.tool.ItemUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Desktop extends Application {

    private double x = 0.00;
    private double y = 0.00;
    private double xOffset = 0;
    private double yOffset = 0;
    private double width = 0.00;
    private double height = 0.00;
    private double resizeWidth = 5.00;
    private double minWidth = 1000.00;
    private double minHeight = 600.00;
    private boolean isMax = false;
    //是否处于右边界调整窗口状态
    private boolean isRight;
    //是否处于下边界调整窗口状态
    private boolean isBottom;
    //是否处于右下角调整窗口状态
    private boolean isBottomRight;


    @Override
    public void start(Stage winStage) throws Exception {
        winStage.initStyle(StageStyle.TRANSPARENT);
        BorderPane mainView = getMainView(winStage);
        winStage.setScene(new Scene(mainView, minWidth, minHeight));
        winStage.setTitle("自定义窗口");
        winStage.getIcons().add(new Image(ItemUtil.DESKTOP_TASK_LOGO));
        doWinStage(winStage);
        doWinState(winStage, mainView);
        winStage.show();
    }


    /**
     * 窗口主体
     */
    public BorderPane getMainView(Stage winStage) {
        BorderPane mainView = new BorderPane();
        mainView.setId("main-view");
        mainView.getStylesheets().add(ItemUtil.DESKTOP_STYLE);
        mainView.setTop(getTopsView(winStage));
        mainView.setCenter(getBodyView());
        return mainView;
    }


    /**
     * 顶部标题栏
     */
    public GridPane getTopsView(Stage winStage) {
        GridPane topsView = new GridPane();
        topsView.setId("tops-view");
        topsView.setHgap(10);
        Label winImage = new Label();
        Label winTitle = new Label();
        Label winItems = new Label();
        Label winAbate = new Label();
        Label winRaise = new Label();
        Label winClose = new Label();
        winTitle.setText("标题信息");
        winImage.setId("tops-view-image");
        winTitle.setId("tops-view-title");
        winItems.setId("tops-view-items");
        winAbate.setId("tops-view-abate");
        winRaise.setId("tops-view-raise");
        winClose.setId("tops-view-close");
        winImage.setPrefSize(27, 23);
        winItems.setPrefSize(27, 23);
        winAbate.setPrefSize(27, 23);
        winRaise.setPrefSize(27, 23);
        winClose.setPrefSize(27, 23);
        topsView.add(winImage, 0, 0);
        topsView.add(winTitle, 1, 0);
        topsView.add(winItems, 2, 0);
        topsView.add(winAbate, 3, 0);
        topsView.add(winRaise, 4, 0);
        topsView.add(winClose, 5, 0);
        topsView.setPadding(new Insets(5));
        topsView.setAlignment(Pos.CENTER_LEFT);
        GridPane.setHgrow(winTitle, Priority.ALWAYS);
        //事件监听
        //1.监听操作选项事件
        winItems.setOnMouseClicked(event -> doWinItems(winItems));
        //2.监听窗口最小事件
        winAbate.setOnMouseClicked(event -> doWinAbate(winStage));
        //3.监听窗口最大事件
        winRaise.setOnMouseClicked(event -> doWinRaise(winStage));
        //4.监听窗口关闭事件
        winClose.setOnMouseClicked(event -> doWinClose(winStage));
        return topsView;
    }


    /**
     * 内容窗体
     */
    public VBox getBodyView() {
        WebView webView = new WebView();
        webView.setCache(false);
        webView.setContextMenuEnabled(false);
        webView.setFontSmoothingType(FontSmoothingType.GRAY);
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.setUserAgent("RedisPlus WebEngine");
        webEngine.load("https://www.baidu.com/");
        return new VBox(webView);
    }


    /**
     * 监听窗口属性事件
     */
    public void doWinStage(Stage winStage) {
        winStage.xProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue != null && !isMax) {
                x = newValue.doubleValue();
            }
        });
        winStage.yProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue != null && !isMax) {
                y = newValue.doubleValue();
            }
        });
        winStage.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue != null && !isMax) {
                width = newValue.doubleValue();
            }
        });
        winStage.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (newValue != null && !isMax) {
                height = newValue.doubleValue();
            }
        });
    }


    /**
     * 监听窗口操作事件
     */
    public void doWinState(Stage winStage, BorderPane mainView) {
        //监听窗口移动后事件
        mainView.setOnMouseMoved((MouseEvent event) -> {
            event.consume();
            double tx = event.getSceneX();//记录x数据
            double ty = event.getSceneY();//记录y数据
            double tw = winStage.getWidth();//记录width数据
            double th = winStage.getHeight();//记录height数据
            //光标初始为默认类型，若未进入调整窗口状态则保持默认类型
            Cursor cursorType = Cursor.DEFAULT;
            //将所有调整窗口状态重置
            isRight = isBottomRight = isBottom = false;
            if (ty >= th - resizeWidth) {
                if (tx <= resizeWidth) {
                    //左下角调整窗口状态
                } else if (tx >= tw - resizeWidth) {
                    //右下角调整窗口状态
                    isBottomRight = true;
                    cursorType = Cursor.SE_RESIZE;
                } else {
                    //下边界调整窗口状态
                    isBottom = true;
                    cursorType = Cursor.S_RESIZE;
                }
            } else if (tx >= tw - resizeWidth) {
                // 右边界调整窗口状态
                isRight = true;
                cursorType = Cursor.E_RESIZE;
            }
            // 最后改变鼠标光标
            mainView.setCursor(cursorType);
        });

        //监听窗口拖拽后事件
        mainView.setOnMouseDragged((MouseEvent event) -> {
            event.consume();
            if (yOffset != 0) {
                winStage.setX(event.getScreenX() - xOffset);
                if (event.getScreenY() - yOffset < 0) {
                    winStage.setY(0);
                } else {
                    winStage.setY(event.getScreenY() - yOffset);
                }
            }
            double tx = event.getSceneX();
            double ty = event.getSceneY();
            //保存窗口改变后的x、y坐标和宽度、高度，用于预判是否会小于最小宽度、最小高度
            double nextX = winStage.getX();
            double nextY = winStage.getY();
            double nextWidth = winStage.getWidth();
            double nextHeight = winStage.getHeight();
            if (isRight || isBottomRight) {
                // 所有右边调整窗口状态
                nextWidth = tx;
            }
            if (isBottomRight || isBottom) {
                // 所有下边调整窗口状态
                nextHeight = ty;
            }
            if (nextWidth <= minWidth) {
                // 如果窗口改变后的宽度小于最小宽度，则宽度调整到最小宽度
                nextWidth = minWidth;
            }
            if (nextHeight <= minHeight) {
                // 如果窗口改变后的高度小于最小高度，则高度调整到最小高度
                nextHeight = minHeight;
            }
            // 最后统一改变窗口的x、y坐标和宽度、高度，可以防止刷新频繁出现的屏闪情况
            winStage.setX(nextX);
            winStage.setY(nextY);
            winStage.setWidth(nextWidth);
            winStage.setHeight(nextHeight);

        });

        //鼠标点击获取横纵坐标
        mainView.setOnMousePressed(event -> {
            event.consume();
            xOffset = event.getSceneX();
            if (event.getSceneY() > 45) {
                yOffset = 0;
            } else {
                yOffset = event.getSceneY();
            }
        });
    }


    /**
     * 监听窗口选项事件
     */
    public void doWinItems(Label winItems) {
        TopsMenu.getInstance().show(winItems, Side.BOTTOM, 5, 6);
    }


    /**
     * 监听窗口最小事件
     */
    public void doWinAbate(Stage winStage) {
        winStage.setIconified(true);
    }


    /**
     * 监听窗口最大事件
     */
    public void doWinRaise(Stage winStage) {
        Rectangle2D rectangle2d = Screen.getPrimary().getVisualBounds();
        isMax = !isMax;
        if (isMax) {
            // 最大化
            winStage.setX(rectangle2d.getMinX());
            winStage.setY(rectangle2d.getMinY());
            winStage.setWidth(rectangle2d.getWidth());
            winStage.setHeight(rectangle2d.getHeight());
        } else {
            // 缩放回原来的大小
            winStage.setX(x);
            winStage.setY(y);
            winStage.setWidth(width);
            winStage.setHeight(height);
        }
    }


    /**
     * 监听窗口关闭事件
     */
    public void doWinClose(Stage winStage) {
        winStage.close();
        Platform.exit();
    }

}