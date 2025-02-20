# 第 12 章 設計とメタファー

## Imposter パターンによって ひらめきを代替する

### 今回の目的

- 通貨の足し算に取り掛かりたい
- まずは、USD 同士の足し算

### 手順

1. テストコードを記述する

   ```java
   public void testSimpleAddition() {
   Money sum = Money.dollar(5).plus(Money.dollar(5));
   assertEquals(Money.dollar(10), sum);
   }
   ```

2. plus() メソッドを実装する

   ```java
   Money plus(Money addend) {
       return new Money(amount + addend.amount, currency);
   }
   ```

### 問題点

- USD 同士の足し算に対応した plus() メソッドの実装は割とシンプルに完了した
- しかし、別通貨同士の足し算には対応できるだろうか？
- 例えば、5USD + 10CHF の足し算には対応できるだろうか？
- Money は、通貨の概念を持つオブジェクトである。これに別通貨同士の足し算の責務を持たせるのは、単一責任の法則に違反する

### Imposter パターン

- ここで、Inposter パターンを採用する
- Imposter パターンとは
  - テスト駆動開発の中で生まれた設計アプローチ
  - 既存のオブジェクトでは、望ましい実装ができない場合、別のオブジェクトに仕事を肩代わりさせる というもの
- Imposter パターンの必要性を説いた Kent Beck の言葉が印象的である
- 「TDD は、設計のひらめきが正しい瞬間に訪れることを保証するものではない。」
  - TDD を習得したからといって、天才的なアイデアが次々に湧くようになるわけではない
  - しかし、小さなサイクルを回し続けることで、ひらめきに頼らなくても 自然と良い設計へと導かれる
  - そのための一つのアプローチが Imposter パターン

### Imposter パターンの視点に立ち、設計を見直す

- 別通貨との足し算を実現するには、以下の２つのオブジェクトが必要そうである。
  - 別通貨同士の計算を式で表現するオブジェクト(Expression)
  - 式を簡約するオブジェクト(Bank)
- この設計したバージョンのテストコードに修正する

### テストコードを修正する

- 修正前

  ```java
   public void testSimpleAddition() {
     Money sum = Money.dollar(5).plus(Money.dollar(5));
     assertEquals(Money.dollar(10), sum);
   }
  ```

- 修正後

  - 以下のように修正した。
  - なお、現状では、未実装により、コンパイルエラーとなるので、これから仮実装を行う

  ```java
  public void testSimpleAddition() {
      Money five = Money.dollar(5);
      Expressson sum = five.plus(five);
      Bank bank = new Bank();
      Money reduced = bank.reduce(sum, "USD");
      assertEquals(Money.dollar(10), reduced);
  }
  ```

  ### 仮実装

  1. Expression インターフェースを作成する

     ```java
     package money;

     public interface Expression {
     }
     ```

  2. Money.plus() が Expression を返すようにする

     - また、このとき、Money は Expression を implements するようにする

       ```java
       Expression plus(Money addend) {
           return new Money(amount + addend.amount, currency);
       }
       ```

  3. Bank クラスを作成し、reduce() メソッドを実装する

     ```java
       package money;

       public class Bank {
           Money reduce(Expression source, String to) {
               return Money.dollar(10);
           }
       }
     ```

  4. テストを実施するとテストが通る！

### 結果

- これで、仮実装を終えることができた
- 次回、リファクタリングを行う
- 現在の ToDo リスト
  - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
  - [ ] 5USD + 5USD = 10USD

# 第 13 章 実装を導くテスト

## ポリモフィズムを活用した設計の洗練

### 今回の論点

- 前回までに、5USD + 5USD = 10USD の仮実装まで進んだ。
- 今回はこれのリファクタリングを行う。

### 設計の方針について

- Expression には Money や Sum などを格納することができる
- Money と Money の足し算は Sum オブジェクトとして表現される
- Expression の中身が Sum オブジェクトであった場合、Bank は足し算の結果を Money に簡約して返す
- Expression の中身が Money オブジェクトであった場合、Bank はそのまま Money オブジェクトを返す

### Sum クラスを作成する

- 上記の要件をもとに、テストコードを追加する

1. テストコードを追加する

   ```java
       public void testPlusReturnsSum() {
           Money five = Money.dollar(5);
           Expression result = five.plus(five);
           Sum sum = (Sum) result;
           assertEquals(five, sum.augend);
           assertEquals(five, sum.addend);
       }
   ```

2. Sum クラスを作成する

   ```java
   package money;

   public class Sum {
       Money augend;
       Money addend;

       Sum(Money augend, Money addend) {
           this.augend = augend;
           this.addend = addend;
       }
   }
   ```

3. テストを実行する

- すると、以下エラーが発生した。
- class money.Money cannot be cast to class money.Sum (money.Money and money.Sum are in unnamed module of loader 'app')
- Money.plus() の戻り値が Sum クラスでないため、(Sum) result できないと言っている。
- 戻り値を Sum 型に変更する

  ```java
      Expression plus(Money addend) {
          return new Sum(this, addend);
      }
  ```

- 無事テストが通った！

### Bank クラスの reduce() メソッドを実装する

- Sum を Money に簡略するような Bank クラスの reduce() メソッドを実装する

1.  テストコードを追加する

    ```java
        public void testReduceSum() {
            Expression sum = new Sum(Money.dollar(3), Money.dollar(4));
            Bank bank = new Bank();
            Money result = bank.reduce(sum, "USD");
            assertEquals(Money.dollar(7), result);
        }
    ```

2.  Bank クラスの reduce() メソッドを実装する

    ```java
        Money reduce(Expression source, String to) {
            Sum sum = (Sum) source;
            int amount = sum.augend.amount + sum.addend.amount;
            return new Money(amount, to);
        }
    ```

    - テストが通った！

3.  リファクタリングする -- Sum クラスの reduce() メソッドを実装する

    - int amount = sum.augend.amount + sum.addend.amount;
    - これは、他クラスのフィールド、さらにはフィールドのフィールドにアクセスしている
    - これは、望ましくないため、これは Sum クラス自身が行うように設計を変更する

    1. Bank クラスの reduce() メソッドを変更する

       ```java
           Money reduce(Expression source, String to) {
               Sum sum = (Sum) source;
               return sum.reduce(to);
           }
       ```

    2. Sum クラスの reduce() メソッドを実装する

       ```java
           Money reduce(String to) {
           int amount = augend.amount + addend.amount;
           return new Money(amount, to);
       }
       ```

4.  リファクタリングする -- Bank クラスの reduce() メソッドを変更する

    - Bank クラスの reduce() メソッドは、現状、引数が Sum クラスの場合しか想定できていない
    - 引数が Money クラスの場合も対応できるようにする

    1. テストコードを追加する

       ```java
           public void testReduceMoney() {
               Bank bank = new Bank();
               Money result = bank.reduce(Money.dollar(1), "USD");
               assertEquals(Money.dollar(1), result);
           }
       ```

    2. Bank クラスの reduce() メソッドを変更する

       ```java
       Money reduce(Expression source, String to) {
           if (source instanceof Money) {
               return (Money) source;
           }
           Sum sum = (Sum) source;
           return sum.reduce(to);
       }
       ```

       - テストが通った！

    3. リファクタリングする
       - クラスチェックによる条件分岐は、ポリモフィズムを利用して代替する
         - Money クラスの reduce() メソッド
           ```java
               Money reduce(String to) {
                   return this;
               }
           ```
         - Sum クラスの reduce() メソッド
           ```java
               Money reduce(String to) {
                   return new Money(amount, to);
               }
           ```
         - Bank クラスの reduce() メソッド
           ```java
               Money reduce(Expression source, String to) {
                   return source.reduce(to);
               }
           ```
         - Expression インターフェース の reduce() メソッド
           ```java
               Money reduce(String to);
           ```
       - 変わらずテストは通った！

### 現在の ToDo リスト

- [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
- [ ] 5USD + 5USD = 10USD
- [ ] 5USD + 5USD が Money を返す
- [ ] Money を変換して換算を行う
- [ ] Reduce(Bank, String)

# 第 14 章 学習用テストと回帰テスト

## 新たな２つのテストの用途

### 今回の目的

- 5USD + 10CHF = 10USD を実現するため、まずは、ある通貨を別のある通貨に変換できるようにしなければならない
- 例えば、10CHF = 5USD のような変換
- この機能は、Bunk クラスに持たせる
- また、この機能を実装するためには、10CHF を 5USD に変換するためのレートが必要である
- レートは通貨のペアをキーとする Hashmap で管理する

### 2CHF = 1USD を確かめるテストを追加する

1. テストコードを追加する

   ```java
    public void testReduceMoneyDifferentCurrency() {
        Bank bank = new Bank();
        bank.addRate("CHF", "USD", 2);
        Money result = bank.reduce(Money.franc(2), "USD");
        assertEquals(Money.dollar(1), result);
    }
   ```

2. bankAddRate() メソッドを実装する

   ```java
       void addRate(String from, String to, int rate) {
           rates.put(new Pair(from, to), rate);
       }
   ```

3. テストを実行する

   - コンパイルが通ったので、テストを実行する
   - もちろん、レッド！

4. エラー文を見てみる

   - AssertionFailedError: expected: <1 USD> but was: <2 CHF>
   - これは、2CHF を 1USD に変換しているはずだが、2CHF のままであることを示している。
   - それもそのはず、Money.reduce() メソッドは、以下のような実装になっているからだ

   ```java
       public Money reduce(String to) {
           return this;
       }
   ```

5. Money クラスの reduce() メソッドを変更する

   ```java
    public Money reduce(String to) {
        int rate = this.currency().equals("CHF") && to.equals("USD") ? 2 : 1;
        return new Money(amount / rate, to);
    }
   ```

   - テスト グリーン！
   - とりあえず、仮実装はできた。

6. リファクタリング

   1. 為替レートは Bank クラスに任せる
      - 現在の設計だと、為替レート int rate は Money クラス自身が把握していることになっている。
      - しかし、これは Bnak クラスの責務のため、Bank クラスに任せる
      - Bunk.rate() メソッド
        ```java
        int rate(String from, String to) {
            int rate = from.equals("CHF") && to.equals("USD") ? 2 : 1;
            return rate;
        }
        ```
      - Money.reduce() メソッド
        ```java
        public Money reduce(String to, Bank bank) {
            int rate = bank.rate(currency, to);
            return new Money(amount / rate, to);
        }
        ```
   2. 為替レートは、Map で管理する

      - 為替レートは、Map に格納する。表で管理するイメージだ。
      - Map<Pair, Integer> rates = new HashMap<>(); とする
      - Pair クラスは、from と to を保持するクラスである

      1. Bank クラスに為替レートを管理するため Map<Pair, Integer> rates を追加する

         ```java
             public class Bank {
                 private Map<Pair, Integer> rates = new HashMap<>();

                 Money reduce(Expression source, String to) {
                     return source.reduce(to, this);
                 }

                 int rate(String from, String to) {
                     return rates.get(new Pair(from, to));
                 }

                 void addRate(String from, String to, int rate) {
                     rates.put(new Pair(from, to), rate);
                 }
             }
         ```

      2. 為替レートを管理するための Pair クラスを作成する

         ```java
             package money;

             public class Pair {
                 private String from;
                 private String to;

                 Pair(String from, String to) {
                     this.from = from;
                     this.to = to;
                 }

                 public boolean equals(Object object) {
                     Pair pair = (Pair) object;
                     return from.equals(pair.from) && to.equals(pair.to);
                 }

                 public int hashCode() {
                     return 0;
                 }
             }
         ```

      3. テストを実行する

         - コンパイルが通ったので、テストを実行する
         - すると、レッド！
         - 原因は、NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "java.util.Map.get(Object)" is null
         - 詳しく見てみると、money.MoneyTest.testReduceMoney(MoneyTest.java:65) つまり、

           ```java
               public void testReduceMoney() {
                   Bank bank = new Bank();
                   Money result = bank.reduce(Money.dollar(1), "USD");
                   assertEquals(Money.dollar(1), result);
               }
           ```

           - で発生している。USD から USD への変換レートを見るけることができなかったことによるエラーのようだ。

      4. Bank.rate() メソッドを変更する

         ```java
             int rate(String from, String to) {
                 if (from.equals(to)) {
                     return 1;
                 }
                 return rates.get(new Pair(from, to));
             }
         ```

         - テストを実行すると、無事通った。

### 学習テスト

- 今回、為替レートを管理する仕組みを設計する上で、当初は from と to を Pair ではなく Object[] で管理しようと想定していた
- しかし、Object[] の equals() の仕様がわからなかったため、一時的に以下の様なテストケースを作成し、テストを行うことで分析した
  ```java
  public void testArrayEquals() {
      assertTrue(new Object[] {"abc"}.equals(new Object[] {"abc"}));
  }
  ```
- すると、テストは失敗する。
- つまり、Object[] は要素の文字列が同じものであっても、false となり、Object そのものが同じでないと true とはならないことがわかる。
- このような、仕様のわからないクラスやメソッドの仕様を明らかにするために一時的に記述するテストケースのことを、学習テストという。

### 回帰テスト

- 今回、6.2.3 では NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "java.util.Map.get(Object)" is null が money.MoneyTest.testReduceMoney(MoneyTest.java:65) で発生した。
- 原因は、USD から USD への変換レートを見るけることができなかったことによるものだった。
- このエラーの修正を確認するために、あるテストケースを書くことにした。

  ```java
  public void testIdentityRate() {
      assertEquals(1, new Bank().rate("USD", "USD"));
  }
  ```

- このように、機能の追加により既存の動作の不具合を防ぐためのテストケースのことを回帰テストという。

# 第 15 章 テスト任せとコンパイラ任せ

## 進むべき道はコンパイラが示してくれる。道を外れたことはテストが教えてくれる。

### 今回の目的

- 前回、10CHF = 5USD のような変換ができるようにした
- ついに、5USD + 10CHF = 10USD の足し算に挑戦する

### 手順

1. テストコードを記述する

   ```java
       public void testMixedAddition() {
           Money fiveBucks = Money.dollar(5);
           Money tenFrancs = Money.franc(10);
           Bank bank = new Bank();
           bank.addRate("CHF", "USD", 2);
           Money result = bank.reduce(fiveBucks.plus(tenFrancs), "USD");
           assertEquals(Money.dollar(10), result);
       }
   ```

   - 以下の様にエラーが帰ってくる
   - testMixedAddition() org.opentest4j.AssertionFailedError: expected: <10 USD> but was: <15 USD>
   - Sum.reduce() の実装に問題あるようなので、この解消に取り組む。

2. Sum.reduce() を変更する。

   - 現状、以下のようになっている
   - augend と addend として受けとったものを reduce() してから足し合わせるようにする。
   - 変更前

   ```java
       public Money reduce(String to, Bank bank) {
           int amount = augend.amount + addend.amount;
           return new Money(amount, to);
       }
   ```

   - 変更後
   - テストが通るようになる！

   ```java
       public Money reduce(String to, Bank bank) {
           int amount = augend.reduce(to, bank).amount + addend.reduce(to, bank).amount;
           return new Money(amount, to);
       }
   ```

### 取り入れられるところには Composite パターンを適用していく

- 引数や戻り値として Money となっている箇所を Expression に置き換えていく。
- これを行うことで、Conposite パターンへの適用を進めていく

1. Sum クラスと Money クラスを Expression インターフェースに対応させる

   - 変更前

     ```java
     package money;

     public class Sum implements Expression {
         Money augend;
         Money addend;

         Sum(Money augend, Money addend) {
             this.augend = augend;
             this.addend = addend;
         }
         ...
     }

     package money;

     public class Money implements Expression {
         protected int amount;
         protected String currency;


         Money times(int multiplier) {
             return new Money(amount * multiplier, currency);
         }

         Money plus(Money addend) {
             return new Money(amount + addend.amount, currency);
         }
         ...
     }
     ```

   - 変更後

     ```java
     package money;

     public class Sum implements Expression {
         Expression augend;
         Expression addend;

         Sum(Expression augend, Expression addend) {
             this.augend = augend;
             this.addend = addend;
         }

         ...
     }

     package money;

     public class Money implements Expression {
         protected int amount;
         protected String currency;


         Expression times(int multiplier) {
             return new Money(amount * multiplier, currency);
         }

         Expression plus(Expression addend) {
             return new Sum(this, addend);
         }
         ...
     }

     ```

2. テストも Expression に対応させる

   - 変更前

     ```java
         public void testMixedAddition() {
             Money fiveBucks = Money.dollar(5);
             Money tenFrancs = Money.franc(10);
             Bank bank = new Bank();
             bank.addRate("CHF", "USD", 2);
             Money result = bank.reduce(fiveBucks.plus(tenFrancs), "USD");
             assertEquals(Money.dollar(10), result);
         }
     ```

   - 変更後

     ```java
         public void testMixedAddition() {
             Expression fiveBucks = Money.dollar(5);
             Expression tenFrancs = Money.franc(10);
             Bank bank = new Bank();
             bank.addRate("CHF", "USD", 2);
             Money result = bank.reduce(fiveBucks.plus(tenFrancs), "USD");
             assertEquals(Money.dollar(10), result);
         }
     ```

   - コンパイラが以下の様に教えてくれる
   - The method plus(Expression) is undefined for the type Expression
   - つまり、Expression には plus(Expression) メソッドがないということだ。
   - そこで、Expression に plus(Expression) メソッドを追加する

3. Expression インターフェースに plus(Expression) メソッドを追加する

   ```java
   package money;

   public interface Expression {
       Money reduce(String to, Bank bank);

       Expression plus(Expression addend);
   }
   ```

4. Money, Sum に plus() を実装にする

   - Money plus() を public にすることで、Expression から見えるようにする
   - Sum に plus() をから実装する

     ```java
         public Expression plus(Expression addend) {
             return null;
         }
     ```

   - テストが通った！

# 第 16 章 将来の読み手を考えたテスト

## 結果を検証するテスト、意図が伝わるテストはドキュメントとして機能する

### 今回の目的

- 前回、Sum クラスの plus() メソッドはから実装だった。それを実装する。
- Sum.times() メソッドの実装も行う

### Sum.plus() を実装する

1. テストコードを記述する

   ```java
    public void testSumPlusMoney() {
        Expression fiveBucks = Money.dollar(5);
        Expression tenFrancs = Money.franc(10);
        Bank bank = new Bank();
        bank.addRate("CHF", "USD", 2);
        Expression sum = new Sum(fiveBucks, tenFrancs).plus(fiveBucks);
        Money result = bank.reduce(sum, "USD");
        assertEquals(Money.dollar(15), result);
    }
   ```

   - 本書では、上記テストコードの前半部分に他のテストコードとの重複が見られるので、フィクスチャの使用が提案されている。

2. テストを実行する

   - テストを実行すると、以下のようなエラーが帰ってくる
   - testSumPlusMoney() java.lang.NullPointerException: Cannot invoke "money.Expression.reduce(String, money.Bank)" because "source" is null
   - これは、Sum.plus() が null を返しているために発生している。

3. Sum.plus() を実装する

   ```java
    public Expression plus(Expression addend) {
        return new Sum(this, addend);
    }
   ```

   - テストを実行すると、テストが通る。

### Sum.times() を実装する

1. テストコードを記述する

   ```java
    public void testSumTimes() {
        Expression fiveBucks = Money.dollar(5);
        Expression tenFrancs = Money.franc(10);
        Bank bank = new Bank();
        bank.addRate("CHF", "USD", 2);
        Expression sum = new Sum(fiveBucks, tenFrancs).times(2);
        Money result = bank.reduce(sum, "USD");
        assertEquals(Money.dollar(20), result);
    }
   ```

2. Sum.times() を実装する

   ```java
    public Expression times(int multiplier) {
        return new Sum(augend.times(multiplier), addend.times(multiplier));
    }
   ```

   - これに付随して、Expression インターフェースに times(int) メソッドを追加する
   - テストを実行すると、テストが通る。

### 将来の読み手を考えたテスト

- テストコードは単なる動作確認のためのものではなく、将来的な開発者の理解を助ける役割を持つべきである。
- テストコードはそのときの開発者だけでなく、将来の開発者が読んでも意図が明確に伝わるように設計すべきである。
- 実装の詳細を検証するテストコードは将来のコード変更の妨げとなってしまう。
- 計算の結果を検証するテストコードを記述すべきである。
- テストコードはドキュメントとしても機能する。読んだだけでプログラムの使用がわかるテストコードと意図が明確なテスト名をつけるようにする。

# 第 16 章 他国通貨全体のふりかえり

### 他国通貨開発の学び

- 変更が多い箇所でこそ TDD は輝く
  å う題材について
  - 今回、他国通過を題材に扱ったが、筆者は何かを執筆する度に TDD を用いてこの他国通過を開発し直してきた。
  - 今回は Expression という発明に至ったが、これは今までにはない発想だった。
  - TDD のテストが先、実装が後という自由度の大きい開発手法だからこその結果だったのではないか。
- 今回の他国通過の開発にあたって、述べ 125 回のテストを実行した。
- プロダクトコードとテストコードの合計行数は同じくらいになった
- テスト駆動開発のプロセス
  - 小さいテストを追加する
  - 全てのテストを動かし、失敗があることを確認する
  - 変更を行う
  - 再び全てのテストを動かし、すべて成功することを確認する
  - リファクタリングを行い重複を除去する
- リファクタリングは小さい変更を身重ねるべき
- テスト駆動開発が担わないもの
  - パフォーマンステスト
  - 負荷テスト
  - ユーザビリティテスト
- 広く知られているテスト評価手法
  - カバレッジ: 厳密なテスト駆動開発では 100%になるべき
  - 欠陥挿入: プロダクトコードの任意の行の意味合いを変えたらテストは失敗するはず
- カバレッジの向上はテストを増やす以外にも、プロダクトコードのリファクタリングで行うこともできる
- テスト駆動開発の３つの驚き
  - 仮実装、三角測量、明白実装という３つのアプローチがある
  - コード間の重複除去によって、設計が駆動していく
  - テストの粒度を調整することでリスクに対応することができる
