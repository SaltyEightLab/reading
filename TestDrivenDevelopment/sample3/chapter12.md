# 第 25 章 テスト駆動開発のパターン

### 悪循環の説明

- Gerry Weinberg の**「Quality Software Management」**の概念を引用して、因果ループを説明。
- ストレスレベルが増加するとどうなるか？
  1. ストレスが増えると、テストをする余裕がなくなる（頻度が減る）。
  2. テストが減ると、エラーが増える。
  3. エラーが増えると、さらにストレスが増す。
  4. これが繰り返され、悪循環に陥る。
     気ｔ

### テストファーストにすることで生まれる好循環

- 「テストファースト」にすると、逆に良い循環になる
- テストを先に書くルールを決めると、悪循環が良い循環に変わる。
  1. 「テストファーストをするとストレスが減る」
  2. 「ストレスが減るともっとテストが書ける」
  3. 「さらにストレスが減る」
  4. これが繰り返され、良い循環になる。
- 結果的に、開発がスムーズに進む！

### 独立したテストの利点

- 各テストの前準備が簡単になり、実行速度が速くなる
- 高凝集、低結合な小さいオブジェクトを設計するようになる

### TODO リストの活用方法

- 「すぐやる」・「あとでやる」・「やらない」に分類する
- 新しいタスクが発生したときに、それをすぐにやるべきか、あとでやるべきか、そもそもやらないのかを素早く決める。
- これにより、重要なタスクに集中しやすくなる。

### アサーションファースト

- 何事も結論から語ると物事がシンプルになる
- テストで言うなれば、それはアサーションから書くということ

* ということは、読み手もアサーションから読んだ方が理解しやすいのでは？

第 26 章 レッドバーのパターン

### 「既知から未知へ」という成長の考え方

- すでにある知識を活かしつつ、未知の領域を切り開くことで、開発を進められる。
- 開発とは、「既知」と「未知」の間を行き来しながら成長するプロセスである。
- 「次に TODO リストの中のどれに取り掛かろう？」と悩んだら、「わかりきってはいないが、書けば動きそうな気がするテスト」に取り掛かろう。

### 学習用テストの主な用途

- 未知の API やフレームワークの動作を理解する
  - 例えば、新しいライブラリやフレームワークを使うとき、いきなり本番コードで使うのではなく、まず 学習用テストを書いて、API の挙動を確認 する。
- パッケージやライブラリのアップデート時の動作検証
  - 依存しているパッケージやライブラリのバージョンが更新されたとき、まず 学習用テストを実行して、既存の動作が変わっていないかを確認 できる。
  - もし、学習用テストが失敗したら、ライブラリの仕様変更が影響している可能性がある。
    - その場合、本番コードの修正が必要になるかもしれない。

### 休憩

1. 休憩の重要性
   - 疲れたり手詰まりになったときは、休憩を取ることが最善の選択肢。
   - 作業から一旦離れることで、新たなアイデアが浮かびやすくなる。
   - 例えば、散歩・昼寝・手を洗うなどで頭をリフレッシュできる。
   - 席を立った瞬間に「このケースを試していなかった！」と気づくこともある。
2. 「シャワーメソッド」
   - Dave Ungar が提唱：分からないときはシャワーを浴びる（思考をリセット）。
   - テスト駆動開発（TDD）はシャワーメソッドの発展形：
     - 何をすべきか分かっているとき → すぐに実装
     - まだ分からないとき → 仮実装や三角測量を行う
     - それでも分からなければ → シャワーを浴びに行く！

### 安い机に良い椅子

- 机はいくらでも拡張することができる
- 椅子はとにかく良いものを使おう。
