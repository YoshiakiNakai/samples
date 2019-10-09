/**
 * Created by Nakai Yoshiaki on 2017/12/03.
 */

public class Skill {

    String ID = "";
    //String learnCond = "";      // スキル習得条件 グローバルフラグcheck
    String name = "";           //名前
    String img = "";            //画像
    String explain = "";        //説明文
    String commandText = "";    //ボタン表示
    String useText = "";        //使用時メッセージ 原文
    String effText = "";        //効果メッセージ 原文
    String waitText = "";       //待機メッセージ 原文
    String useDispText = "";       //表示用テキスト
    String effDispText = "";       //表示用テキスト
    String waitDispText = "";

    String category = "";       //スキル効果分類
    String sort = "";           // 種類   //アイテム、技能

    String explainEff = "";

    int price = 0;
    //int sellPrice = 0;

    //消費リソース
    int needTime = 0;       //発動に必要な時間
    int spendTime = 0;
    int spendmaxHP = 0;
    int spendnHP = 0;
    int spendnMP = 0;
    int spendnAtk = 0;
    int spendnDef = 0;
    int spendnSp = 0;

    // 待機中補正値
    //String waitEff = "";
    //String waitPow = "";
    String waitAbno = "";
    //int wAtk = 0;
    int wDef = 0;

    //対象リソース
    String objRes = "";     //対象リソース 原文
    String objValue = "";   //その効果量  原文
    String objMagnify = ""; //倍率    原文

    //実際の効果量
    int effTime = 0;
    int effnHP = 0;
    int effmaxHP = 0;
    int effnMP = 0;
    int effnAtk = 0;
    int effnDef = 0;
    int effnSp = 0;

    // 威力
    int pownTime = 0;   // ごろ的にpownにしよう
    int powmaxHP = 0;
    int pownHP = 0;
    int pownMP = 0;
    int pownAtk = 0;
    int pownDef = 0;
    int pownSp = 0;

    //対象の抵抗値
    String resistText = "";
    int resistValue = 0;

    // 状態異常付与
    String abnoText = "";

    //フラグ操作
    String putGFlag = "";
}
