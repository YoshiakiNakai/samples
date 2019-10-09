import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;   //広告
//import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

//    private AdView mAdView; //広告

    private MediaPlayer _player;
    private String bgmName = "";
    private int bgmRePos = 0;

    //-----------UI部品-----------------
    private RelativeLayout layoutStart;
    private LinearLayout layoutGame, layoutCanvas;
    private CanvasBasicView canvasMain;
    private float objectScale = 1;// enemyに適用するscaleを最初に表示する画像を使って求める
    private float charachipScale = 1;
    //int mapY=0;  // マップ画像移動値   // gcdにした

    //private ListView lvStatus;
    private TextView txtMain, txtStatus;
    //private ImageView imgMain;
    private Button btnStart, btn1, btn2, btn3, btn4, btn5;

    //-----------ゲーム処理データ-----------------
    CommonData gcd = new CommonData();
    ArrayList<ArrayList<String>> extFileData = new ArrayList<>();
    Map<String, GameState> mStates = new HashMap<>();
    Stack<GameState> sStack = new Stack<>();
    AsyncProcess async = new AsyncProcess();

    //-----------------onCreate-----------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        //広告表示処理
//        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));
//        mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        _player = new MediaPlayer();

        // UI部品準備
        layoutStart = (RelativeLayout) findViewById(R.id.layoutStart);
        layoutGame = (LinearLayout) findViewById(R.id.layoutGame);

        canvasMain = new CanvasBasicView(this);
        layoutCanvas = (LinearLayout) findViewById(R.id.layoutCanvas);
        layoutCanvas.addView(canvasMain);
        //lvStatus = (ListView) findViewById(R.id.lvStatus);

        btnStart = (Button) findViewById(R.id.btnStart);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
        btn5 = (Button) findViewById(R.id.btn5);

        txtMain = (TextView) findViewById(R.id.txtMain);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        //imgMain = (ImageView) findViewById(R.id.imgMain);

        // 音楽再生準備
        //_player = new MediaPlayer();
        //String mediaFileUriStr = "android.resource://" + getPackageName() + "/" + R.raw.city;  // （6）
        //Uri mediaFileUri = Uri.parse(mediaFileUriStr);  // （7）
        //URI文字列    //URI 名前や場所を識別するための書き方のルール、URLもURIの一つ
        //アプリ内のリソース音声ファイルを表すURI文字列は
        // android.resource://アプリのルートパッケージ/リソースファイルのR値
        /*
        try {   //非同期で再生の準備を行うためtry内に記述
            _player.setDataSource(MainActivity.this, mediaFileUri);  //再生するファイルの指定
            _player.setOnPreparedListener(new PlayerPreparedListener());  //再生準備が終わったときに呼ばれる関数の登録
            _player.setOnCompletionListener(new PlayerCompletionListener());  //再生が終了したときに呼ばれる関数の登録
            _player.prepareAsync();  //非同期で再生準備を行う  //ファイルを読み込むなど
        }
        catch (IOException e) {
        }*/


        // GameStateの登録
        GameState GS;
        GS = new ISdungeon();
        mStates.put("ISdungeon", GS);
        GS = new ISchatEvent();
        mStates.put("ISchatEvent", GS);
        GS = new ISbattle();
        mStates.put("ISbattle", GS);
        GS = new ISmenu();
        mStates.put("ISmenu", GS);
        GS = new ISshop();
        mStates.put("ISshop", GS);
        GS = new ISsetSkill();
        mStates.put("ISsetSkill", GS);
        GS = new ISskiShop();
        mStates.put("ISskiShop", GS);


        // constファイルの読み込み   <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        load_constFileData("skill_list.csv");
        load_constFileData("equip_list.csv");
        load_constFileData("enemy_list.csv");
        load_constFileData("abno_list.csv");    //状態異常ファイル



        // ゲームの再現
        loadSave();
    }

    //-----------ボタン処理-----------------
    public void onClickButton(View view) {
        switch (view.getId()) {
            case R.id.btnStart:    //ゲームスタート
                //System.out.println((String)btnStart.getText());
                gameStart( (String)btnStart.getText());
                break;
            case R.id.btn1:
                gameExec( (String)btn1.getText());
                break;
            case R.id.btn2:
                gameExec( (String)btn2.getText());
                break;
            case R.id.btn3:
                gameExec( (String)btn3.getText());
                break;
            case R.id.btn4:
                gameExec( (String)btn4.getText());
                break;
            case R.id.btn5:
                gameExec( (String)btn5.getText());
                break;

        }
    }

    @Override
    public void onPause() {
        _player.stop();  // 再生を止める

//        //広告とめる
//        if (mAdView != null) {
//            mAdView.pause();
//        }
        super.onPause();
    }

    @Override
    public void onResume() {
        _player.start();    // 再生再開

//        //広告再開
//        if (mAdView != null) {
//            mAdView.resume();
//        }
        super.onResume();
    }

    // 音楽再生 ------------------------------------------- onDestroy()
    //Activity終了時に MediaPlayerオブジェクトを解放させる
    protected void onDestroy() {
//        // 広告破棄
//        if (mAdView != null) {
//            mAdView.destroy();
//        }

        // 音楽stop
        if(_player.isPlaying()) {
            _player.stop();  // 再生を止める
        }
        _player.release();  // 開放する
        _player = null;  // 一応、nullを放り込んどく

        super.onDestroy();
    }

    //再生準備がおわったときに呼ばれる関数
    private class PlayerPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
        }
    }

    //再生が終わったときに呼ばれる関数
    private class PlayerCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if(!_player.isLooping()) {  //ループ再生していなければ
                _player.seekTo(bgmRePos);  //再生位置の指定
                _player.start();        // 再生する
            }        }
    }

    // 音楽処理
    void bgmPlay(){
        String bgm = sStack.peek().bgm;
        if(bgm.length()== 0) return;
        if(bgm.equals(bgmName)) return;
        bgmName = bgm;
        bgmRePos = sStack.peek().bgmRePos;  // リピート位置
        int bgmId = getResources().getIdentifier(bgm, "raw", getPackageName());// 画像idの取得

        //String mediaFileUriStr = "android.resource://" + getPackageName() + "/" + bgmId;  // （6）
        //String mediaFileUriStr = "android.resource://" + getPackageName() + "/" + R.raw.city;  // （6）
        //Uri mediaFileUri = Uri.parse(mediaFileUriStr);  // （7）

        if(_player.isPlaying()) {
            _player.stop();
            _player.release();
        }
        _player = MediaPlayer.create(getApplicationContext(), bgmId);
        _player.setOnCompletionListener(new PlayerCompletionListener());  //再生が終了したときに呼ばれる関数の登録
        //_player.setLooping(true);
        /*
        _player = new MediaPlayer();
        try {   //非同期で再生の準備を行うためtry内に記述
            _player.setDataSource(MainActivity.this, mediaFileUri);  //再生するファイルの指定
            _player.setOnPreparedListener(new PlayerPreparedListener());  //再生準備が終わったときに呼ばれる関数の登録
            _player.setOnCompletionListener(new PlayerCompletionListener());  //再生が終了したときに呼ばれる関数の登録
            _player.prepareAsync();  //非同期で再生準備を行う  //ファイルを読み込むなど
        }
        catch (IOException e) {
        }*/

        _player.start();        // 再生する
    }



    //=====================ゲーム処理================================

    //-----------スタート処理-----------------
    void gameStart(String btn){
        // FrameLayout切り替え
        layoutStart.setVisibility(View.INVISIBLE);
        layoutGame.setVisibility(View.VISIBLE);
        layoutCanvas.setVisibility(View.VISIBLE);

        /*
        // 物体画像の拡大率を決める
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.title);
        //float cw = layoutCanvas.getWidth();
        float ch = layoutCanvas.getHeight();
        //float bw = bmp.getWidth();
        float bh = bmp.getHeight();
        objectScale = ch/bh;
        */

        //ゲーム処理
        sStack.peek().sExec(btn);
        gcd = sStack.peek().gcd;    // gcdの更新（参照渡し）   //sExecのあと必ず行う
        checkNextState();       // State遷移を行う
        displayUpdate();
    }

    //-----------ゲーム実行-----------------
    void gameExec(String btn){
        sStack.peek().sExec(btn);
        gcd = sStack.peek().gcd;    // gcdの更新   //sExecのあと必ず行う
        checkNextState();       // State遷移を行う
        async.checkBtnAble();  // バトル処理
        displayUpdate();

        saveData();// データのセーブ
    }

    //------------ゲームのセーブ---------------------
    public void saveData(){
        String save = "";
        String deliD = ",";
        for(Skill s: gcd.charaMem.get(0).skills){
            save += s.ID + " ";
        } save = save.substring(0, save.length()-1); save += deliD;
        for(Skill s: gcd.charaMem.get(0).learnedAt){
            save += s.ID + " ";
        } save = save.substring(0, save.length()-1); save += deliD;
        for(Skill s: gcd.charaMem.get(0).learnedMg){
            save += s.ID + " ";
        } save = save.substring(0, save.length()-1); save += deliD;
        for(Skill s: gcd.charaMem.get(0).learnedGd){
            save += s.ID + " ";
        } save = save.substring(0, save.length()-1); save += deliD;
        for(Skill s: gcd.charaMem.get(0).learnedSp){
            save += s.ID + " ";
        } save = save.substring(0, save.length()-1); save += deliD;
        save += gcd.money; save += deliD;
        for(Object obj: gcd.intGFlag.entrySet()){   // <key, value>の組を全て取り出す
            Map.Entry entry = (Map.Entry)obj;
            save += entry.getKey() + " " + entry.getValue() + "&";
        } save = save.substring(0, save.length()-1); save += deliD;
        for(Object obj: gcd.strGFlag.entrySet()){   // <key, value>の組を全て取り出す
            Map.Entry entry = (Map.Entry)obj;
            save += entry.getKey() + " " + entry.getValue() + "&";
        } save = save.substring(0, save.length()-1); //save += deliD;

        outputData("save0", save);
    }

    //------------ゲームのロード---------------------
    public void loadSave(){
        String load = inputSave("save0");   //セーブデータ読み込み
        // ゲームの再現
        if(load.equals("") == false) {  //セーブデータから再現
            // キャラ再現
            Character chara = new Character();
            chara.ID = "私";
            chara.name = "私";//load;
            chara.HP = 100;
            chara.MP = 0;
            chara.atk = 0;
            chara.def = 0;
            chara.sp = 5;

            String[] loadData = load.split(",");
            String[] sIds = loadData[0].split(" ");
            String[] leAtids = loadData[1].split(" ");
            String[] leMgids = loadData[2].split(" ");
            String[] leGdids = loadData[3].split(" ");
            String[] leSpids = loadData[4].split(" ");

            //戦闘スキル
            chara.skills.add(gcd.setSkill(sIds[0]));
            chara.skills.add(gcd.setSkill(sIds[1]));
            chara.skills.add(gcd.setSkill(sIds[2]));
            chara.skills.add(gcd.setSkill(sIds[3]));
            chara.skills.add(gcd.setSkill(sIds[4]));
            //所持スキル
            for(int i=0; i<leAtids.length; i++){
                chara.learnedAt.add(gcd.setSkill(leAtids[i]));
            }
            for(int i=0; i<leMgids.length; i++){
                chara.learnedMg.add(gcd.setSkill(leMgids[i]));
            }
            for(int i=0; i<leGdids.length; i++){
                chara.learnedGd.add(gcd.setSkill(leGdids[i]));
            }
            for(int i=0; i<leSpids.length; i++){
                chara.learnedSp.add(gcd.setSkill(leSpids[i]));
            }

            gcd.charaMem.add(chara);
            gcd.charaMem.get(0).calcEquipStatus();  //能力値の初期化
            gcd.recoverCharaMemStatus();

            gcd.story = 1;
            gcd.money = Integer.valueOf(loadData[5]);
            String[] loadIFlag = loadData[6].split("&");
            for(int i=0; i<loadIFlag.length; i++){
                String[] ki = loadIFlag[i].split(" ");
                gcd.intGFlag.put(ki[0], Integer.valueOf(ki[1]));
            }
            gcd.intGFlag.put("戦闘数値説明", -1);

            String[] loadSFlag = loadData[7].split("&");
            for(int i=0; i<loadSFlag.length; i++){
                String[] ki = loadSFlag[i].split(" ");
                gcd.strGFlag.put(ki[0], ki[1]);
            }

            // マップ再現
            gcd.nextState = "push=ISdungeon";
            gcd.extFileNames = "map_act.csv&map_situ.csv";
            gcd.tmpLocal = "街";
            load_extFileData();     // gcd.extFileNames のファイルを extFileDataに読み込む
            toNextState();          // gcd.nextState から StateStackに GameStateを設定する
        }
        else{ //ゲーム初期化
            // キャラ再現
            Character chara = new Character();
            chara.ID = "私";
            chara.name = "私";//load;
            chara.HP = 100;
            chara.MP = 0;
            chara.atk = 0;
            chara.def = 0;
            chara.sp = 5;
            chara.skills.add(gcd.setSkill("ダガー"));
            chara.skills.add(gcd.setSkill("ロングソード"));
            chara.skills.add(gcd.setSkill("レザーシールド"));
            chara.skills.add(gcd.setSkill("ヒート"));
            chara.skills.add(gcd.setSkill("様子を見る"));

            chara.learnedAt.add(gcd.setSkill("ダガー"));
            chara.learnedAt.add(gcd.setSkill("ロングソード"));
            chara.learnedGd.add(gcd.setSkill("レザーシールド"));
            chara.learnedMg.add(gcd.setSkill("ヒート"));
            chara.learnedSp.add(gcd.setSkill("様子を見る"));

            gcd.charaMem.add(chara);
            gcd.charaMem.get(0).calcEquipStatus();  //能力値の初期化
            gcd.recoverCharaMemStatus();

            gcd.money = 15;
            gcd.story = 1;
            gcd.intGFlag.put("戦闘数値説明", -1);
            gcd.strGFlag.put("init", "0");
            // マップ再現
            gcd.nextState = "push=ISdungeon";
            gcd.extFileNames = "map_act.csv&map_situ.csv";
            gcd.tmpLocal = "街";
            load_extFileData();     // gcd.extFileNames のファイルを extFileDataに読み込む
            toNextState();          // gcd.nextState から StateStackに GameStateを設定する

        }
    }


    //=========================バトル処理（非同期処理）============================
    // UIの操作をこのスレッドで行うなら MainActivityに作る必要がある
    class AsyncProcess extends Handler {    // HandlerとMessageによる非同期処理

        private Message msg = Message.obtain();	// Messageのインスタンス化

        @Override
        public void handleMessage(Message msg) {	//handleMessage() Messageを受け取ったときに呼ばれる関数
            this.removeMessages(0);   //既存のmessageを削除 //指定されたwhat値をもつメッセージを削除
            checkNextState();       // State遷移を行う
            switch(sStack.peek().btnAbleFlag){    // ボタンenable設定
                case("wait"):   // 待機、ボタンを押せない
                    sStack.peek().sLoopExec("AsyncExe");    // messageが残ったまま、状態遷移すると別のStateのsLoopExec()が実行される
                    sendMessageDelayed(obtainMessage(0), sStack.peek().loopDelayTime);  //ミリ秒後にメッセージを出力
                    displayUpdate();    //UI表示
                    break;
                default:    // 行動選択
                    displayUpdate();    //UI表示
                    break;  // handleMessageを抜け出す

            }   //obtainMessage(int what): Messageクラスのフィールド値whatの設定
        }	   //switch文でwhat値により場合分けすることも可能

        // 非同期処理を開始するか判断    // btnAbleFlagがwaitなら開始
        public void checkBtnAble(){
            if(sStack.peek().btnAbleFlag.equals("wait")){
                handleMessage(msg);   //実行
            }
        }
    };



    //----------ディスプレイ更新処理-------------
    public void displayUpdate(){
        // StateStackの一番上を表示する
        String mainText = sStack.peek().mainText;
        String[] btnTexts = sStack.peek().btnTexts;
        String statusText = sStack.peek().getStatusText();

        drawCanvasMain();    //canvas描画
        txtMain.setText(mainText);

        if(statusText.length() != 0) {
            txtStatus.setText(statusText);
        }

        btn1.setText(btnTexts[0]);
        btn2.setText(btnTexts[1]);
        btn3.setText(btnTexts[2]);
        btn4.setText(btnTexts[3]);
        btn5.setText(btnTexts[4]);
        setEnableBtns();    //ボタンenable設定

        bgmPlay();  // 音楽変更確認
    }

    //----------Canvasに表示するbitmapのサイズ調整-------------
    void drawCanvasMain() { // mainImg : 背景画像
        if(sStack.peek().mainImg.length() == 0) return; // 背景がなかったらreturn

        // 変数準備
        float cw = layoutCanvas.getWidth();
        float ch = layoutCanvas.getHeight();
        float cvsScale = ch / cw;
        float tmpScale = 0;
        Matrix tmpMatrix = new Matrix();  //変換行列の用意     // post, setがある、postは掛け算、setは代入
        Bitmap bgRsz;
        // canvasのリセット
        canvasMain.resetCanvas();

        // 背景の描画----------------------------------
        String mainImg = sStack.peek().mainImg;
        if (mainImg.length() != 0) {

            // 画像idの取得
            int imgId = getResources().getIdentifier(mainImg, "drawable", getPackageName());// R.drawable.filename
            //bitmapの取得
            Bitmap rawBg = BitmapFactory.decodeResource(getResources(), imgId);

            float bw = rawBg.getWidth();
            float bh = rawBg.getHeight();

            switch (sStack.peek().canvasFlag) {
                default:
                    //縦幅を合わせた背景描画
                    tmpScale = ch / bh;
                    tmpMatrix.setScale(tmpScale, tmpScale);     // 比率をMatrixに設定
                    // リサイズ画像取得   // 元のbmp, トリミング始点x, y, 切り出す幅 width, height, 変換行列, filterをかけるか
                    bgRsz = Bitmap.createBitmap(rawBg, 0, 0, rawBg.getWidth(), rawBg.getHeight(), tmpMatrix, true);
                    int bgx = (int) (layoutCanvas.getWidth() - bgRsz.getWidth()) / 2;     // x座標表示位置を画面の中央にする
                    canvasMain.setBitmap(bgRsz, bgx, 0);        //描画
            /*
            //横幅を合わせた描画
            tmpScale = cw / bw;
            tmpMatrix.setScale(tmpScale, tmpScale);
            bgRsz = Bitmap.createBitmap(rawBg, 0, 0, rawBg.getWidth(), rawBg.getHeight(), tmpMatrix, true);
            canvasMain.setBitmap(bgRsz, bgx, 0);        //描画
            */
            }
        }

        // 物体の描画 --------------------------------------
        String objImg = sStack.peek().objectImg;
        if (objImg.length() != 0) { //画像が指定されていれば

            // 物体の描画
            int oId = getResources().getIdentifier(objImg, "drawable", getPackageName());// 画像idの取得
            Bitmap objbmp = BitmapFactory.decodeResource(getResources(), oId);    //bitmapの取得
            tmpScale = objectScale;   // あらかじめ計算しておいた倍率を設定
            tmpMatrix.setScale(tmpScale, tmpScale);    // 比率をMatrixに設定
            Bitmap eRsz = Bitmap.createBitmap(objbmp, 0, 0, objbmp.getWidth(), objbmp.getHeight(), tmpMatrix, true);
            int ex = (int) (layoutCanvas.getWidth() - eRsz.getWidth()) / 2;     // x座標表示位置を画面の中央にする
            int ey = (int) (layoutCanvas.getHeight() - eRsz.getHeight())*3/4;    // y座礁表示位置
            //int ey = (int) (layoutCanvas.getHeight() - eRsz.getHeight());    // y座礁表示位置は下端に合わせる
            canvasMain.setBitmap(eRsz, ex, ey);
        }

        // キャラ画像の描画 ----------------------------------
        String personImg = sStack.peek().personImg;
        if (personImg.length() != 0) { //画像が指定されていれば

            int oId = getResources().getIdentifier(personImg, "drawable", getPackageName());// 画像idの取得
            Bitmap objbmp = BitmapFactory.decodeResource(getResources(), oId);    //bitmapの取得
            tmpScale = charachipScale;
            tmpMatrix.setScale(tmpScale, tmpScale);    // 比率をMatrixに設定
            // chara chipの前向き直立部分を切り取る
            Bitmap eRsz = Bitmap.createBitmap(objbmp, objbmp.getWidth()/3, 0, objbmp.getWidth()/3, objbmp.getHeight()/4, tmpMatrix, true);
            int ex = (int) (layoutCanvas.getWidth() - eRsz.getWidth()) / 2;     // x座標表示位置
            int ey = (int)(layoutCanvas.getHeight() - eRsz.getHeight());    // y座礁表示位置
            canvasMain.setBitmap(eRsz, ex, ey);   //敵のみの描画

        }

        // 会話キャラの描画 ----------------------------------
        String chatImg = sStack.peek().chatImg;
        if (chatImg.length() != 0) { //画像が指定されていれば

            int oId = getResources().getIdentifier(chatImg, "drawable", getPackageName());// 画像idの取得
            Bitmap objbmp = BitmapFactory.decodeResource(getResources(), oId);    //bitmapの取得
            tmpScale = charachipScale;
            tmpMatrix.setScale(tmpScale, tmpScale);    // 比率をMatrixに設定
            Bitmap eRsz = Bitmap.createBitmap(objbmp, objbmp.getWidth()/3, 0, objbmp.getWidth()/3, objbmp.getHeight()/4, tmpMatrix, true);
            int ex = 0; //(int) (layoutCanvas.getWidth() - eRsz.getWidth()) / 2;     // x座標表示位置
            int ey = (int)(layoutCanvas.getHeight() - eRsz.getHeight());    // y座礁表示位置
            canvasMain.setBitmap(eRsz, ex, ey);   //敵のみの描画

        }

        canvasMain.draw();  // 描画

    }


    //----------ボタンenable設定-------------
    void setEnableBtns(){
        switch(sStack.peek().btnAbleFlag) {
            case("onlyRtn"):
                btn1.setEnabled(false);
                btn2.setEnabled(false);
                btn3.setEnabled(false);
                btn4.setEnabled(false);
                btn5.setEnabled(true);
                break;

            case ("wait"):  // wait時    //全て押せない状態にする
                btn1.setEnabled(false);
                btn2.setEnabled(false);
                btn3.setEnabled(false);
                btn4.setEnabled(false);
                btn5.setEnabled(false);
                break;

            default:    // 通常時
                if (btn1.getText().length() != 0) {
                    btn1.setEnabled(true);
                } else {
                    btn1.setEnabled(false);
                }
                if (btn2.getText().length() != 0) {
                    btn2.setEnabled(true);
                } else {
                    btn2.setEnabled(false);
                }
                if (btn3.getText().length() != 0) {
                    btn3.setEnabled(true);
                } else {
                    btn3.setEnabled(false);
                }
                if (btn4.getText().length() != 0) {
                    btn4.setEnabled(true);
                } else {
                    btn4.setEnabled(false);
                }
                if (btn5.getText().length() != 0) {
                    btn5.setEnabled(true);
                } else {
                    btn5.setEnabled(false);
                }
                break;
        }   // end switch()
    }


    //-----------State遷移を行うか確認する-----------------
    void checkNextState(){
        if(gcd.nextState.length() == 0) return;     // State遷移があるか確認    // pop, push=ISchat
        if(gcd.extFileNames.length() != 0)          // 外部ファイル読み込みがあるか確認
            load_extFileData();     // gcd.extFileNames のファイルを extFileDataに読み込む
        if( toNextState()) {          // gcd.nextState から StateStackに GameStateを設定する
            // push, change のときは遷移先Stateのゲーム処理を行う
            sStack.peek().sExec("Initial"); // 新しいStateの初実行
            gcd = sStack.peek().gcd;    // gcdの更新
        }else{
            sStack.peek().sReStart("ReStart");  // pop後の前処理
        }
        checkNextState();   // 遷移後、再度チェック
        return;
    }

    //-----------State遷移処理-----------------
    public boolean toNextState(){   //戻り値: falseならこのあと画面描画のみ。trueなら再実行要求
        // 参照渡しチェック
        //System.out.println(gcd.nextState);
        //System.out.println(sStack.peek().gcd.nextState);
        //gcd.nextState = "";
        //System.out.println(gcd.nextState);
        //System.out.println(sStack.peek().gcd.nextState);
        String[] ns = gcd.nextState.split("=");   //例、push=chatEvent

        //処理を判定
        if(ns[0].equals("pop")){    // popのとき
            sStack.pop();
            sStack.peek().gcd = gcd; // gcdデータの変化を反映
            gcd.nextState = "";
            return false;
        }else if(ns[0].equals("push")){ // pushのとき
            mStates.get( ns[1]).init(gcd, extFileData);  // GameStateを準備
            sStack.push(mStates.get( ns[1]));           // StateStackにpush
            gcd.nextState = "";
        }else if(ns[0].equals("change")){   // changeのとき
            mStates.get( ns[1]).init(gcd, extFileData);  // GameStateを準備
            sStack.pop();                                  // StateStackに popしてから
            sStack.push(mStates.get( ns[1]));           // push
            gcd.nextState = "";
        }
        return true;
    }

    //-----------外部データ読み込み-----------------
    public void load_extFileData() {
        if (gcd.extFileNames.length() == 0) return;

        extFileData.clear();    // 前のデータを消去

        // 一つずつファイルを読み込む
        String loadList[] = gcd.extFileNames.split("&");
        for (int i = 0; i < loadList.length; i++) {
            System.out.println(loadList[i]);
            ArrayList<String> fileData = loadGameFile(loadList[i]);
            extFileData.add(fileData);
        }
        gcd.extFileNames = "";

        return;
    }

    //------constFileData読み込み-------
    public void load_constFileData(String filename) {
        ArrayList<String> fileData = loadGameFile(filename);
        gcd.constFileData.add(fileData);

        return;
    }


    // --------ファイル読み込み----------------
    public ArrayList<String> loadGameFile(String gamefile){
        InputStream is = null;
        BufferedReader br = null;
        ArrayList<String> eventList = new ArrayList<String>();

        try {
            try {

                is = this.getAssets().open(gamefile);
                br = new BufferedReader(new InputStreamReader(is, "SJIS"));

                // １行ずつ読み込み、改行を付加する
                String str;
                while ((str = br.readLine()) != null) {
                    eventList.add( str); //行ごとにArrayListに入れる
                }
            } finally {
                if (is != null) is.close();
                if (br != null) br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            finish();   //エラー終了
        }

        return eventList;
    }

    //------------セーブデータの出力---------------------
    public void outputData(String file, String str) {

        try (FileOutputStream fos = openFileOutput(file, Context.MODE_PRIVATE);){

            fos.write(str.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //------------セーブデータの読み込み---------------------
    public String inputSave(String file) {
        String text = "";

        // try-with-resources
        try (FileInputStream fis = openFileInput(file);
             BufferedReader reader= new BufferedReader(
                     new InputStreamReader(fis,"UTF-8"))
        ) {

            String lineBuffer;
            while ((lineBuffer = reader.readLine()) != null) {
                text += lineBuffer;
            }
        } catch (NullPointerException e){  //ファイルがないときは nullを返す
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text;
    }

}


    /*
    //----------ステータス表示作成-------------
    String setStatusText(){
        String sts = "";

        // 表示形式の選択      //最初から、gameStateに処理を渡しとけばよかったな、Overrideしたいならどうぞって感じで
        switch(sStack.peek().statusDispFlag) {
            case("battle"): // 戦闘用表示
                sts = sStack.peek().getStatusText();
                break;

            default:    // 通常時
                for(int mn=0; mn<gcd.charaMem.size(); mn++) {
                    Character chara = gcd.charaMem.get(mn);
                    sts += "なまえ:" + chara.name;
                    sts += "  深度: " + gcd.story;
                    sts += "\nHP:" + chara.nHP + "/" + chara.maxHP;
                    sts += "  MP:" + chara.nMP + "/" + chara.maxMP;
                    sts += "\n攻:" + chara.nAtk;
                    sts += "  防:" + chara.nDef;
                    sts += "  速:" + chara.nSp;

                    sts += "\n武器: " + chara.weapon;
                    sts += "\n防具: " + chara.protec;

                    sts += "\n結晶: " + gcd.money + "欠片";
                    sts += "\n持ち物";
                    for (int i = 0; i < gcd.bel.size(); i++) {
                        if (i % 2 == 0) sts += "\n";
                        else sts += "  ";
                        sts += gcd.bel.get(i);
                    }
                }
                break;

        }
        return sts;
    }
    */


    /* imageViewでの画像移動
    //-----------画像移動-----------------
    void moveImg() {
        //画像をbitmapに変換して読み込み
        Bitmap bmg = BitmapFactory.decodeResource(getResources(), R.drawable.imp0087h);

        System.out.println(imgMain.getWidth());
        System.out.println(imgMain.getHeight());
        System.out.println(bmg.getWidth());
        System.out.println(bmg.getHeight());

        float iw = imgMain.getWidth();
        float ih = imgMain.getHeight();
        float bw = bmg.getWidth();
        float bh = bmg.getHeight();

        // 比率を求める
        float scale = iw/bw;
        iw /= scale;
        ih /= scale;
        bw *= scale;    //変換後の幅
        bh *= scale;
        //変換行列の用意
        Matrix matrix = new Matrix();

        // 比率をMatrixに設定
        matrix.postScale(scale, scale);
        System.out.println(matrix);
        // リサイズ画像   // 元のbmp, トリミング始点x, y, 切り出す幅 width, height, 変換行列, filterをかけるか
        Bitmap bmpRsz = Bitmap.createBitmap(bmg, 0, imgY, (int)iw , (int)ih, matrix, true);
            // 切り出す幅、width, heightは bmgのwidthとheightを使って画像全体を切り出しやすいように、matrixの変換を受けるようになっている
            // 今回は imageViewの大きさに合わせて切り出したいので、matrixの変換をもとに戻して切り出す

        System.out.println(bmpRsz.getWidth());
        System.out.println(bmpRsz.getHeight());

        imgMain.setImageBitmap(bmpRsz);

        //imgMain.setScrollY(imgY);
        imgY += 10;
        if( (imgY+(ih*scale)) > bh) imgY = 0;   //画像はみ出し確認

    }
    */
