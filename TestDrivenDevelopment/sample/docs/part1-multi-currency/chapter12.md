# 第 12 章 設計とメタファー

##

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
