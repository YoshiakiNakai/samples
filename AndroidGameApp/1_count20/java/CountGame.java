package nakai.counting;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import java.util.Random;
/*
Random r = new Random();
int n = r.nextInt(30) + 1;
 */

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;   //広告
import com.google.android.gms.ads.MobileAds;

public class CountGame extends AppCompatActivity {

    private AdView mAdView; //広告

    //-----------------------UI用変数---------------------------------------------------
    private LinearLayout layoutStart;   //画面下のボタンのレイアウト
    private LinearLayout layoutGame;
    private LinearLayout layoutEnd;

    private TextView txtCounter, txtDisp, txtP1, txtP2;    //数字表示用のTextView, 文字表示用のTextView
    private ImageView imgBg;        //背景表示のためのImageView
    private Button btnCount1, btnCount2, btnEnd1, btnEnd2, btnOnemore, btnFinish;

    //-----------------------ゲーム処理用変数---------------------------------------------------
    int counter;    //数えている数字
    int finishNum;  //言ったら終わる数
    int tmpCount;   //現在連続で数えている回数
    int tmpLimit;   //連続で数えられる数
    int turn;       //フラグ、誰のターンか    //1:P1, 2:P2, 3:CPU
    int cpuMode=0;   //vsPlayer か vsCPUか      //Interface化しようとしても UI系の処理をここでしたいから難しい
    CountAI cpu = new CountAI();

    public void Init() {
        counter = 0;
        finishNum = 20;
        tmpCount = 0;
        tmpLimit = 3;
        turn = 1;
    }

    //-----------------------処理用変数----------------------------------------------
    final Handler handler = new Handler(Looper.getMainLooper());	//メインスレッドの登録


    //-----------------------onCreate()---------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_game);

        // xmlから Viewの取得
        layoutStart = (LinearLayout) findViewById(R.id.layoutStart);
        layoutGame = (LinearLayout) findViewById(R.id.layoutGame);
        layoutEnd = (LinearLayout) findViewById(R.id.layoutEnd);

        txtCounter = (TextView) findViewById(R.id.txtCounter);
        txtDisp = (TextView) findViewById(R.id.txtDisp);
        txtP1 = (TextView) findViewById(R.id.txtP1);
        txtP2 = (TextView) findViewById(R.id.txtP2);

        imgBg = (ImageView) findViewById(R.id.imgBg);
        btnCount1 = (Button) findViewById(R.id.count1);
        btnCount2 = (Button) findViewById(R.id.count2);
        btnEnd1 = (Button) findViewById(R.id.end1);
        btnEnd2 = (Button) findViewById(R.id.end2);
        btnOnemore = (Button) findViewById(R.id.onemore);
        btnFinish = (Button) findViewById(R.id.finish);

        //広告表示処理
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_unit_id));
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



        //txtCounter.append("0");
        //layoutBackground.setBackgroundResource(R.drawable.game_bg);  //背景画像の変更
    }
    //-----------------------onCreate() END---------------------------------------------------

    //ActivityのlifeCycleに合わせて広告表示するために Activityのメソッドに広告の処理を追加しておく
    @Override   //onPause()が呼ばれたとき、広告もonPause()する
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    //-----------------------onClickButton()---------------------------------------------------
    public void onClickButton(View view) {
        switch (view.getId()) {
            case R.id.vsPla:    //ゲームスタート
                layoutStart.setVisibility(View.INVISIBLE);
                layoutGame.setVisibility(View.VISIBLE);
                layoutEnd.setVisibility(View.INVISIBLE);
                CountGame.this.Init();    //game用変数の初期化
                cpuMode = 0;
                setBtnState();              //Buttonの状態をセットする
                txtCounter.setText(String.valueOf(counter));
                txtP1.setText("Player1");
                txtP2.setText("Player2");
                break;
            case R.id.vsCom:    //Computerモード
                layoutStart.setVisibility(View.INVISIBLE);
                layoutGame.setVisibility(View.VISIBLE);
                layoutEnd.setVisibility(View.INVISIBLE);
                CountGame.this.Init();    //game用変数の初期化
                cpuMode = 1;
                setBtnState();              //Buttonの状態をセットする
                txtCounter.setText(String.valueOf(counter));
                txtP1.setText("YOU");
                txtP2.setText("CPU");
                break;
            case R.id.count1:   //プレイヤーの動作
                CountGame.this.count();
                setBtnState();
                break;
            case R.id.count2:
                CountGame.this.count();
                setBtnState();
                break;
            case R.id.end1:
                CountGame.this.turnEnd();
                break;
            case R.id.end2:
                CountGame.this.turnEnd();
                break;
            case R.id.onemore:
                layoutStart.setVisibility(View.VISIBLE);
                layoutGame.setVisibility(View.INVISIBLE);
                layoutEnd.setVisibility(View.INVISIBLE);
                imgBg.setImageResource(R.drawable.game_bg);
                txtCounter.setText(String.valueOf(finishNum));
                txtDisp.setText("");
                break;
            case R.id.finish:
                finish();
                break;

        }
    }


    //-----------------------ゲーム処理関数---------------------------------------------------
    //ボタンの状態をセットする
    public void setBtnState() {
        switch (turn) {               //break; を忘れないこと
            case 1:     //Player1
                if (tmpCount == 0) {
                    btnCount1.setEnabled(true);     //C1
                    btnCount2.setEnabled(false);    //C2
                    btnEnd1.setEnabled(false);      //E1
                    btnEnd2.setEnabled(false);      //E2
                } else if (tmpCount < tmpLimit) {
                    btnCount1.setEnabled(true);
                    btnCount2.setEnabled(false);
                    btnEnd1.setEnabled(true);
                    btnEnd2.setEnabled(false);
                } else {
                    btnCount1.setEnabled(false);
                    btnCount2.setEnabled(false);
                    btnEnd1.setEnabled(true);
                    btnEnd2.setEnabled(false);
                }
                break;

            case 2:     //Player2
                if (tmpCount == 0) {
                    btnCount1.setEnabled(false);
                    btnCount2.setEnabled(true);
                    btnEnd1.setEnabled(false);
                    btnEnd2.setEnabled(false);
                } else if (tmpCount < tmpLimit) {
                    btnCount1.setEnabled(false);
                    btnCount2.setEnabled(true);
                    btnEnd1.setEnabled(false);
                    btnEnd2.setEnabled(true);
                } else {
                    btnCount1.setEnabled(false);
                    btnCount2.setEnabled(false);
                    btnEnd1.setEnabled(false);
                    btnEnd2.setEnabled(true);
                }
                break;

        }

    }

    //数を数える
    public void count() {
        counter += 1;
        tmpCount += 1;
        txtCounter.setText(String.valueOf(counter));
        judgeEnd();
    }

    //ターンを終える           //cpuModeにより挙動が異なる
    public void turnEnd() {
        tmpCount = 0;

        if(cpuMode == 0) {  //対人戦のとき
            switch (turn) {
                case 1:
                    turn = 2;
                    break;
                case 2:
                    turn = 1;
                    break;
            }
            setBtnState();

        }else {              //CPU戦のとき
            switch (turn) {
                case 1:
                    turn = 3;
                    btnCount1.setEnabled(false);
                    btnCount2.setEnabled(false);
                    btnEnd1.setEnabled(false);
                    btnEnd2.setEnabled(false);
                    cpu.start();        //cpuのターン処理
                    break;

                case 3:
                    turn = 1;
                    setBtnState();
                    break;

            }
        }
    }

    //終了判定とその処理を行う
    public void judgeEnd() {
        if (counter == finishNum) {
            btnOnemore.setEnabled(false);
            btnFinish.setEnabled(false);
            layoutStart.setVisibility(View.INVISIBLE);
            layoutGame.setVisibility(View.INVISIBLE);
            layoutEnd.setVisibility(View.VISIBLE);
            imgBg.setImageResource(R.drawable.game_set_bg2);
            switch (turn) {
                case 1:
                    if(cpuMode == 0) {
                        txtCounter.setText("");
                        txtDisp.setText("Player1\nLOST");
                    }else{
                        txtCounter.setText("");
                        txtDisp.setText("YOU\nLOST");
                    }
                    break;
                case 2:
                    txtCounter.setText("");
                    txtDisp.setText("Player2\nLOST");
                    break;
                case 3:
                    txtCounter.setText("");
                    txtDisp.setText("YOU\nWIN");
                    break;
            }
        }

            //wait処理    // 別スレッドを起動
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    }catch(InterruptedException e) {
                    }
                    handler.post(new Runnable(){
                        @Override
                        public void run(){
                            btnOnemore.setEnabled(true);
                            btnFinish.setEnabled(true);
                        }
                    });
                }
            }).start();
        }


    //---------------------AIの実装----------------------------------------------------------
    public class CountAI extends Handler {
        private Message msg = Message.obtain();
        private int drawFlag;
        private int loopNum;    //
        Random random = new Random();

        public CountAI(){
            drawFlag = 0;
        }

        @Override
        public void handleMessage(Message msg){
            this.removeMessages(0);
            switch(drawFlag){   //flagを順に進めていき、描画処理を逐次行う
                case 0: //defaultが何もしない状態の方がいいんだけど
                    break;
                case 1:
                    loopNum = think();    //数える回数を求める
                    drawFlag++;
                    if(counter == finishNum-1)
                        this.sendMessageDelayed(obtainMessage(0), 2000);
                    else
                        this.sendMessageDelayed(obtainMessage(0), 1000);
                    break;
                default:
                    if(drawFlag-2 < loopNum){
                        count();
                        this.sendMessageDelayed(obtainMessage(0), 500);
                    }else{  //数え終わったら
                        turnEnd();
                    }
                    drawFlag++;
                    break;
            }
        }

        //数える回数を求める
        private int think() {
            int loop=1;    //数える回数
            if(counter == finishNum-1) {
                return 1;
            }
            switch(counter % 4){
                case 0:
                    loop=3;
                    break;
                case 1:
                    loop=2;
                    break;
                case 2:
                    loop=1;
                    break;
                case 3://必勝できないとき
                    loop = random.nextInt(3) + 1;   //数える回数をランダムに決定
                    break;
            }
            return loop;
        }

        public void start(){
            drawFlag = 1;
            handleMessage(msg);
        }
    }

}

//wait処理
    /*      thread は終了後、使いまわすことができない
    Thread endWait = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                endWait.sleep(1000);
            }catch(InterruptedException e) {
            }
            handler.post(new Runnable(){
                @Override
                public void run(){
                    btnOnemore.setEnabled(true);
                    btnFinish.setEnabled(true);
                }
            });
        }
    });*/
