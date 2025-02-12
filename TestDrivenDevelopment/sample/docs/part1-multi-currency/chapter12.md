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
  - 別通貨同士の計算を式表現するオブジェクト(Expression)
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
