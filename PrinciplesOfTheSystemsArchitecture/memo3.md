# Chapter 5 アプリケーション機能を組み立てる

## ドメインオブジュエクトを使って機能を実現する

### 三層 + ドメインモデル設計におけるアプリケーション層の役割

業務ロジックはドメインモデルに含まれているので、この設計においてアプリケーション層は、プレゼンテーション層やドメインオブジェクト、データソース層との連絡、指示を行うマネージャーのような役割を担う。よって、マネージャーであるアプリケーション層が、判断・加工・計算を行ってはいけない。

## サービスクラスを作りながらドメインモデルを改善する

### サービスクラスに業務ロジックを書きそうになったら・・・

サービスクラスに業務ロジックを書きそうになることがある。しかし、これはマネージャーに雑務を行わせるようなものだ。だから、行ってはいけない。
業務ロジックはドメインオブジェクトに持たせる。もし、その業務ロジックの適切な実装先がドメインオブジェクトに見当たらないのであれば、既存のドメインクラスを変更したり、新たに追加する良い機会だ。

### テスト駆動開発との類似点

ここでは、
① 業務ロジックを手っ取り早くサービスクラスに書いてしましたい。
② しかし、それではコードの重複が発生する。
③ 適切な実装場所を考える。
④ 実装場所として適切なドメインオブジェクトがないのであれば、追加する。

という手順が紹介されている。
私は、この書籍を読む前、書籍「テスト駆動開発」を読んでいたが、
その書籍の中で、
① まずは仮実装を行う。
② コードの重複が発生する。
③ コードの重複の削減を考える。つまり、重複したコードの移設先を考える。
④ 移設先がないのなら、Imposter パターンを採用し、その振る舞いを新たなクラスに担わせる。

というプラクティスを学んだ。
この２つのプラクティスは非常によく似ていると思う。

## 画面の多様な要求を小さく分けて整理する

### プレゼンテーション層にアプリケーション層が振り回されてしまう例

プレゼンテーション層で様々な検索機能が利用できるとする。例えば、商品名による検索、金額による検索、特徴による検索などだ。
このような様々な検索機能を単一の API でまとめようとしてしまうと、アプリケーション層に検索機能を切り替えるための複雑な条件分岐分が生じる。
だから、そうではなく、アプリケーション層の検索ロジック自体を小分けにすることで、プレゼンテーション層からは、それぞれの検索ロジックを呼び出すだけにする。
そうすることで、アプリケーション層をシンプルに保つことができる。

### ３層構造 + ドメインモデルとは

一般的な 「三層構造＋ドメインモデル」 は、通常は以下の構成

| 層の名前             | 実装例（Spring の場合）           | 役割                                  |
| -------------------- | --------------------------------- | ------------------------------------- |
| プレゼンテーション層 | Controller                        | HTTP リクエストの受付、画面遷移の制御 |
| アプリケーション層   | Service                           | 業務ロジックの流れを制御              |
| ドメイン層           | Domain (Entity、ValueObject など) | ビジネスの本質的なルールを記述        |
| データソース層       | Repository                        | データベースや永続化処理を担当        |

※多くの Spring の書籍やチュートリアルでは、
「プレゼンテーション層＝ Controller」を指しており、「フロントエンド（React など）」は「プレゼンテーション層のさらに外側」に位置づけられている。

### アプリケーション層とプレゼンテーション層の間の約束事

🔹 サービスクラスを提供する側（アプリケーション層）は…

- 「null を渡さない／null を返さない」

      -nullが渡された時点で処理を拒否し、呼び出し元がnullを渡さないように約束する。
      - 自分自身もnullを返さないと約束することで、呼び出し元はnullチェックを不要にする。

-「状態に依存する場合、使う側が事前に確認する」

      - 例えば「引き出しが可能か」を事前に確認してから「引き出し処理」を行う。
      - サービス提供側は状態の検証を繰り返さず、単純に処理を実行するだけになる。

-「約束を守ったうえでさらに異常が起きた場合は、例外で通知する」

      - 呼び出し元（プレゼンテーション層）が上記の約束を守っていても、異常が起きる場合は例外を投げることで異常を通知する。

## データベースの都合から分離する

### データベースの影響を受けないために

しばしばプログラムの構造がデータベースの影響を受けてしまうことがある。
そうならないために、業務の視点からの記録と参照の関心ごとは、リポジトリとしてドメイン層に宣言する。
そして、リポジトリの実装をインフラ層にて行うようにする。

```
src/main/java
└── com
    └── example
        └── banking
            |// Spring Bootの起動クラス
            ├── BankingApplication.java
            |// @SpringBootApplication
            |// public class BankingApplication {
            |//     public static void main(String[] args) {
            |//         SpringApplication.run(BankingApplication.class, args);
            |//     }
            |// }
            |
            |// プレゼンテーション層：HTTP要求受付・レスポンス生成
            ├── controller
            |  └── BankAccountController.java
            |   // @RestController
            |   // public class BankAccountController {
            |   //     @Autowired
            |   //     BankAccountScenario scenario;
            |   //
            |   //     @PostMapping("/withdraw")
            |   //     public ResponseEntity<?> withdraw(@RequestBody WithdrawRequest request){
            |   //         Amount amount = scenario.withdraw(request.getAmount());
            |   //         return ResponseEntity.ok(amount);
            |   //     }
            |   // }
            |
            |// アプリケーション層：業務ロジック・処理の制御
            ├── application
            │   ├── scenario（複合サービスをまとめる）
            │   │   └── BankAccountScenario.java
            │   │    // @Service
            │   │    // public class BankAccountScenario {
            │   │    //     @Autowired BankAccountService queryService;
            │   │    //     @Autowired BankAccountUpdateService updateService;
            │   │    //
            │   │    //     public Amount withdraw(Amount amount){
            │   │    //         if (!queryService.canWithdraw(amount)){
            │   │    //             throw new IllegalStateException("残高不足");
            │   │    //         }
            │   │    //         updateService.withdraw(amount);
            │   │    //         return queryService.balance();
            │   │    //     }
            │   │    // }
            │   └── service（個別のサービスクラス）
            │        ├── BankAccountService.java
            │        └── BankAccountUpdateService.java
            |
            |// ドメイン層：業務ルール・状態を表現、リポジトリの抽象定義
            ├── domain
            │   ├── model（業務ルールや状態の定義）
            │   │    ├── BankAccount.java
            │   │    │// public class BankAccount {
            │   │    │//     private Amount balance;
            │   │    │//     public boolean canWithdraw(Amount amount) {...}
            │   │    │//     public void withdraw(Amount amount) {...}
            │   │    │// }
            │   │    └── Amount.java
            │   │
            │   └── repository（DB操作の抽象化）
            │        └── BankAccountRepository.java
            │         // public interface BankAccountRepository {
            │         //     Amount balance();
            │         //     void withdraw(Amount amount);
            │         // }
            |
            |// インフラ層：DBアクセスの実装
            └── infrastructure
                └── datasource
                      └── BankAccountDatasource.java
                       // @Repository
                       // public class BankAccountDatasource implements BankAccountRepository {
                       //     public Amount balance(){
                       //         // DB処理
                       //     }
                       //     public void withdraw(Amount amount){
                       //         // DB処理
                       //     }
                       // }

src/main/resources
├── application.yml（アプリケーション設定）
└── schema.sql（DBスキーマ）

src/test/java（単体テスト）
└── com.example.banking
       ├── controller
       ├── application
       ├── domain
       └── infrastructure
```
