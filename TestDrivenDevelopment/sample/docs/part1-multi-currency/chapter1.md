- # 第１章 仮実装

## テスト駆動開発 はじめの一歩

### スタンス

- テスト駆動開発では、基本的に「どんなテストが通ったときに、目的とするコードが完成したと言えるのか？」と考える
- つまり、テストを先に考えるということ
- 目の前の仕事が複雑なときは、サイクルを細かくする
- そして、テスト駆動開発では、後述する小さなサイクルを回し続けることが大切

### ToDo リストを活用する

- 「こんなテストをしないといけないなぁ。」と思いついたら、それを ToDo リストに残していくようにする
- ToDo リストの中の、解決できそうなものから取り組んでいく

### テスト駆動開発における仮実装の小さなサイクル

1. 小さいテストを１つ書く
2. 全てのテストを実行し、１つ失敗することを確認する
3. 失敗を解消するための小さい変更を行う
4. 再テストを行い、すべて成功することを確認する
5. リファクタリングを行い、重複を除去する

### ToDo リスト

- 現在の ToDo リストは以下の通り
  - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
  - [x] 5USD \* 2 = 10USD
  - [ ] amount を private にする
  - [ ] Dollar の副作用どうする？
  - [ ] Money の丸め処理どうする？

### サンプルコード

- 現段階でのコードは以下の通り

```java
package money;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MoneyTest {
    @Test
    public void testMultiplication() {
        Dollar five = new Dollar(5);
        five.times(2);
        assertEquals(10, five.amount);
    }
}
```

```java
package money;

public class Dollar {
    int amount;

    Dollar(int amount) {
        this.amount = amount;
    }

    void times(int multiplier) {
        amount *= multiplier;
    }
}
```

# 第 2 章 明白な実装

## 正しい実装が分かるなら、明白な実装を行う

### 仮実装と明白な実装の違い

- 仮実装とは、キレイな実装方法がわからない場合に、とにかくテストが通るように、とりあえず書き、それからリファクタリングをしていくことで形を整えていく方法
- 明白な実装とは、綺麗な実装方法がアイデアとして、既に頭のある場合に、それをそのまま形にする方法

### 今回の課題

- 以下のコードから分かる通り、Dollar five の状態が times() によって変更されてしまっている。
- これは副作用と言われる望ましくない結果である
- できれば、オブジェクトの状態を変えるのではなく、欲しい結果は戻り値として受け取れるようにしたい。

```java
package money;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MoneyTest {
    @Test
    public void testMultiplication() {
        Dollar five = new Dollar(5);
        five.times(2);
        assertEquals(10, five.amount);
    }
}
```

### 課題に対して明白な実装を行う

1. 望ましい状態を期待したものに、テストコードを改変する
2. まずは、return null; のような空実装でよいので、times() を変更し、コンパイルを通す。
3. 正しいと思える実装を適用（明白な実装）し、テストを通す。

### 上記を行った結果

- 現在のコード

  ```java
  package money;

  import org.junit.jupiter.api.Test;
  import static org.junit.jupiter.api.Assertions.*;

  public class MoneyTest {
      @Test
      public void testMultiplication() {
          Dollar five = new Dollar(5);
          Dollar product = five.times(2);
          assertEquals(10, product.amount);
          product = five.times(3);
          assertEquals(15, product.amount);
      }
  }
  ```

  ```java
  package money;

  public class Dollar {
      int amount;

      Dollar(int amount) {
          this.amount = amount;
      }

      Dollar times(int multiplier) {
          return new Dollar(amount * multiplier);
      }
  }
  ```

- 現在の ToDo リスト
  - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
  - [x] 5USD \* 2 = 10USD
  - [ ] amount を private にする
  - [x] Dollar の副作用どうする？
  - [ ] Money の丸め処理どうする？

# 第 3 章 三角測量

## 三角測量の手順とその必要性

### 今回の問題点

- Money は値オブジェクトである。
- 現実世界の通貨と同じく、製造番号等が違えど、5 ドルは他の 5 ドルと等しいと判断されなければならない
- しかし、現状では、five.equals(new Dollar(5)) は false となってしまう
- これが、true になるようする

### 三角測量とは

- テスト駆動開発における小さなサイクルの回し方の３つ目として三角測量という方法がある
- 仮実装は正しい実装のアイデアがない場合に、とりあえずテストが通るように書き、リファクタリングしていく手法
- 明白な実装は正しい実装のアイデアがある場合に、それをそのまま形にする手法
- 三角測量とは以下のような手法である
  - 正しい実装のアイデアがない場合に採用する
  - まずは、仮実装と同じく、テストケースを１つ用意し、それを通すように実装する
  - 次に、テストケースを追加する。実装を追加したテストケースにも通るよう改変していく
  - 必要ならば、テストケースの実装と実装の改変を繰り返していく。
  - このサイクルを経て、実装が一般化されていく
- あまり、一般的に用いられる手法ではないが、どうしても実装のアイデアが浮かばない場合に、違う角度から物事を考えるよいきっかけけになる

### 三角測量の手順

1. 以下のテストメソッドを MoneyTest に追加する。現状これは失敗する、

   ```java
       @Test
       public void testEquality() {
           assertTrue(new Dollar(5).equals(new Dollar(5)));
       }
   ```

2. 以下のように、Dollar クラスに equals() メソッドを追加する。これは空の実装でよい。

   ```java
       @Override
       public boolean equals(Object object) {
           return true;
       }
   ```

3. 以下のように、２つ目のテストケースを追加する。

   ```java
       @Test
       public void testEquality() {
           assertTrue(new Dollar(5).equals(new Dollar(5)));
           assertFalse(new Dollar(5).equals(new Dollar(6)));
       }
   ```

4. 以下のように、Dollar クラスに equals() メソッドを改変する。これにより、テストケースが通り、equals() メソッドが正しく動作するようになる。

   ```java
       @Override
       public boolean equals(Object object) {
           Dollar dollar = (Dollar) object;
           return amount == dollar.amount;
       }
   ```

### 結果

- 現在のコード

  ```java
  package money;

  import org.junit.jupiter.api.Test;
  import static org.junit.jupiter.api.Assertions.*;

  public class MoneyTest {
      @Test
      public void testMultiplication() {
          Dollar five = new Dollar(5);
          Dollar product = five.times(2);
          assertEquals(10, product.amount);
          product = five.times(3);
          assertEquals(15, product.amount);
      }

      @Test
      public void testEquality() {
          assertTrue(new Dollar(5).equals(new Dollar(5)));
          assertFalse(new Dollar(5).equals(new Dollar(6)));
      }
  }
  ```

  ```java
  package money;

  public class Dollar {
      int amount;

      Dollar(int amount) {
          this.amount = amount;
      }

      Dollar times(int multiplier) {
          return new Dollar(amount * multiplier);
      }

      @Override
      public boolean equals(Object object) {
          Dollar dollar = (Dollar) object;
          return amount == dollar.amount;
      }
  }
  ```

- 現在の ToDo リスト
  - null との比較や、他クラスとのオブジェクトとの比較等も求められがちだが、現状不要なので、ToDo に追加しておくのみとする
    - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
    - [x] 5USD \* 2 = 10USD
    - [ ] amount を private にする
    - [x] Dollar の副作用どうする？
    - [ ] Money の丸め処理どうする？
    - [x] equals() メソッドを実装する
    - [ ] hashCode() メソッドを実装する
    - [ ] null との等価性比較
    - [ ] 他クラスとのオブジェクトとの等価性比較

# 第 4 章 意図を語るテスト

## テストコードを通していかにメソッドの意図を伝えるか

### 今回の目的

- 現状のテストコードからは、dollar.times() は Dollar を返すという意図が読み取れない
- テストコードからその意図が読み取れるようにする

### テストコードの改変

1. 10 や 15 といった数値では、Dollar のオブジェクトを返すという意図が読み取れない

   ```java
       public void testMultiplication() {
           Dollar five = new Dollar(5);
           Dollar product = five.times(2);
           assertEquals(10, product.amount);
           product = five.times(3);
           assertEquals(15, product.amount);
       }
   ```

2. new Dollar(10) や new Dollar(15) といったコードでは、Dollar のオブジェクトを返すという意図が読み取れるようにする

   ```java
       public void testMultiplication() {
           Dollar five = new Dollar(5);
           Dollar product = five.times(2);
           assertEquals(new Dollar(10), product);
           product = five.times(3);
           assertEquals(new Dollar(15), product);
       }
   ```

3. product を five.times(2) や five.times(3) に変更し、意図をわかりやすくする

   ```java
       public void testMultiplication() {
           Dollar five = new Dollar(5);
           assertEquals(new Dollar(10), five.times(2));
           assertEquals(new Dollar(15), five.times(3));
       }
   ```

4. amount を他クラスが呼び出すこともなくなったので、private にする。

   ```java
   package money;

   public class Dollar {
       private int amount;

       Dollar(int amount) {
           this.amount = amount;
       }

       Dollar times(int multiplier) {
           return new Dollar(amount * multiplier);
       }

       @Override
       public boolean equals(Object object) {
           Dollar dollar = (Dollar) object;
           return amount == dollar.amount;
       }
   }
   ```

### 結果

- 現在の ToDo リスト
  - null との比較や、他クラスとのオブジェクトとの比較等も求められがちだが、現状不要なので、ToDo に追加しておくのみとする
    - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
    - [x] 5USD \* 2 = 10USD
    - [x] amount を private にする
    - [x] Dollar の副作用どうする？
    - [ ] Money の丸め処理どうする？
    - [x] equals() メソッドを実装する
    - [ ] hashCode() メソッドを実装する
    - [ ] null との等価性比較
    - [ ] 他クラスとのオブジェクトとの等価性比較

# 第 5 章 原則をあえて破るとき

## とりあえずテストを通す、その後でリファクタリングをしっかり行う

### 今回の目的

1. 現状の ToDo リスト

   - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
   - [x] 5USD \* 2 = 10USD
   - [x] amount を private にする
   - [x] Dollar の副作用どうする？
   - [ ] Money の丸め処理どうする？
   - [x] equals() メソッドを実装する
   - [ ] hashCode() メソッドを実装する
   - [ ] null との等価性比較
   - [ ] 他クラスとのオブジェクトとの等価性比較

2. 何に取り組むか

   - 本当は、5USD + 10CHF = 10USD (レートが 2:1 の場合) に挑戦したいが、まだ挑める気がしない
   - スモールステップを踏む必要がある
   - Todo に 5CHF \* 2 = 10CHF を追加し一旦これをクリアする

### とりあえず、テストケースを追加する

- 以下のテストケースを追加する

  ```java
      @Test
      public void testFrancMultiplication() {
          Franc five = new Franc(5);
          assertEquals(new Franc(10), five.times(2));
          assertEquals(new Franc(15), five.times(3));
      }
  ```

- これにより、Franc クラスが必要であることが分かる
- すでに、Dollar のための public void testMultiplication() があることから、これはテストコードの重複ではないかと思われるかもしれない
- しかし、テスト駆動開発においては、以下のサイクルを回し続けることが大切だということを思い出そう
  1. 小さいテストを１つ書く
  2. 全てのテストを実行し、１つ失敗することを確認する
  3. 失敗を解消するための小さい変更を行う
  4. 再テストを行い、すべて成功することを確認する
  5. リファクタリングを行い、重複を除去する
- 大切なのは、このサイクルの 4 までをできるだけ早く終わらせ、5 をしっかりと行うことである
- よって、今回の場合も、一旦重複を許し、5 をしっかりと行うことを優先する

### とりあえず、Franc クラスを作成する

- 以下のように、Franc クラスを作成する
- ほぼ、Dollar クラスと同じである

  ```java
    package money;

    public class Franc {
        private int amount;

        Franc(int amount) {
            this.amount = amount;
        }

        Franc times(int multiplier) {
            return new Franc(amount * multiplier);
        }

        @Override
        public boolean equals(Object object) {
            Franc franc = (Franc) object;
            return amount == franc.amount;
        }
    }
  ```

### 現在の ToDo リスト

- 現在の ToDo リストは以下の通りである
- 一旦、Dollar と Franc の重複を許した代わりに、この重複を取り除くこと等をタスクとして受け入れた
  - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
  - [x] 5USD \* 2 = 10USD
  - [x] amount を private にする
  - [x] Dollar の副作用どうする？
  - [ ] Money の丸め処理どうする？
  - [x] equals() メソッドを実装する
  - [ ] hashCode() メソッドを実装する
  - [ ] null との等価性比較
  - [ ] 他クラスとのオブジェクトとの等価性比較
  - [x] 5CHF \* 2 = 10CHF
  - [ ] Dollar と Franc の重複を除去する
  - [ ] equals() の一般化
  - [ ] times() の一般化

# 第 6 章 テスト不足に気づいたら

##

### 今回の目的

- 前回、サイクルの 4 までをできるだけ早く終わらせ、5 をしっかりと行うことを優先した
- 今回は 5 の重複を除去する作業、リファクタリングを行う
- 重複を除去するにあたって、どのような方法を採用するか検討する必要がある
- Dollar と Franc の親クラスとして Money クラスを作成し、それぞれがこれを継承する形にする

### 重複を除去する

1. Money クラスを作成する

   ```java
   package money;

   public class Money {

   }
   ```

2. Dollar が Money を継承するように変更する

   - 以下のように継承を宣言する
   - ここで、一度テストを実行してみる。テストが通ることを確認する

     ```java
     public class Dollar extends Money
     ```

3. Dollar から Money に amout の宣言を移動する

   - 以下のように、Dollar から Money に amout の宣言を移動する
   - テストが通ることを確認する

     ```java
     package money;

     public class Money {
         protected int amount;
     }
     ```

4. Dollar から Money に equals() メソッドを移動する

   1. まずは、Dollar の equals() メソッドを Money に移動しても問題ないように変更する

      ```java
        public boolean equals(Object object) {
            Money money = (Money) object;
            return amount == money.amount;
        }
      ```

   2. テストを実行し、テストが通ることを確認する
   3. 実際に、Money に equals() メソッドを移動する
   4. テストを実行し、テストが通ることを確認する

5. Franc にも同じことを行う

   1. ここで、Franc では equals() メソッドの等価性テストを行っていないことに気が付く
   2. このような場合は、後からでもよいので、「これをやるべきだった！」と思えるテストを書く
   3. Dollar の equals()メソッドの等価性テストのコピー&ペーストでいい。あとでテストの重複を取り除く
      ```java
        public void testEquality() {
        assertTrue(new Dollar(5).equals(new Dollar(5)));
        assertFalse(new Dollar(5).equals(new Dollar(6)));
        assertTrue(new Franc(5).equals(new Franc(5)));
        assertFalse(new Franc(5).equals(new Franc(6)));
        }
      ```
   4. Franc で extend Money を宣言し、 amout の宣言と equals() メソッドを 削除する

      ```java
        package money;

        public class Franc extends Money {
            Franc(int amount) {
                this.amount = amount;
            }

            Franc times(int multiplier) {
                return new Franc(amount * multiplier);
            }
        }
      ```

6. テストを行ってみる。全て問題なく通る

### 現状の確認

- 現状、各クラスは以下のようになっている

  ```java
  package money;

  import org.junit.jupiter.api.Test;
  import static org.junit.jupiter.api.Assertions.*;

  public class MoneyTest {
      @Test
      public void testMultiplication() {
          Dollar five = new Dollar(5);
          assertEquals(new Dollar(10), five.times(2));
          assertEquals(new Dollar(15), five.times(3));
      }

      @Test
      public void testEquality() {
          assertTrue(new Dollar(5).equals(new Dollar(5)));
          assertFalse(new Dollar(5).equals(new Dollar(6)));
          assertTrue(new Franc(5).equals(new Franc(5)));
          assertFalse(new Franc(5).equals(new Franc(6)));
      }

      @Test
      public void testFrancMultiplication() {
          Franc five = new Franc(5);
          assertEquals(new Franc(10), five.times(2));
          assertEquals(new Franc(15), five.times(3));
      }
  }
  ```

  ```java
  package money;

  public class Dollar extends Money {

      Dollar(int amount) {
          this.amount = amount;
      }

      Dollar times(int multiplier) {
          return new Dollar(amount * multiplier);
      }
  }
  ```

  ```java
  package money;

  public class Franc extends Money {
      Franc(int amount) {
          this.amount = amount;
      }

      Franc times(int multiplier) {
          return new Franc(amount * multiplier);
      }
  }
  ```

  ```java
  package money;

  public class Money {
      protected int amount;

      public boolean equals(Object object) {
          Money money = (Money) object;
          return amount == money.amount;
      }
  }
  ```

- ToDo リストは以下の通り
  - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
  - [x] 5USD \* 2 = 10USD
  - [x] amount を private にする
  - [x] Dollar の副作用どうする？
  - [ ] Money の丸め処理どうする？
  - [x] equals() メソッドを実装する
  - [ ] hashCode() メソッドを実装する
  - [ ] null との等価性比較
  - [ ] 他クラスとのオブジェクトとの等価性比較
  - [x] 5CHF \* 2 = 10CHF
  - [ ] Dollar と Franc の重複を除去する
  - [x] equals() の一般化
  - [ ] times() の一般化
  - [ ] Franc と Dollar を比較する

# 第 7 章 疑念をテストに翻訳する

## Dollar と Franc が同一の価値と判断されていはマズい

### 今回の目的

- 現状、Dollar と Franc の比較について全く考慮されていない。
- Dollar と Franc が同価値になってしまっては問題なのだが、どうなのだろう？
- テストにして確かめてみる。

### テストを追加する

- 以下のように Dollar と Franc の比較を行うテストを追加する
- 現状ではこのテストは失敗する。
- つまり、Dollar と Franc が同価値になってしまっていることが分かる。
  ```java
      public void testEquality() {
          assertTrue(new Dollar(5).equals(new Dollar(5)));
          assertFalse(new Dollar(5).equals(new Dollar(6)));
          assertTrue(new Franc(5).equals(new Franc(5)));
          assertFalse(new Franc(5).equals(new Franc(6)));
          assertFalse(new Dollar(5).equals(new Franc(5)));
      }
  ```

### Money の equals() メソッドを修正する

- 以下のように equals() メソッドを修正する
- これにより、同じクラスのインスタンス同士でしか比較をしないようになる
- Dollar と Franc の比較自体を拒むことができる
- テストを実行すると、テストは通る

  ```java
      public boolean equals(Object object) {
          Money money = (Money) object;
          return amount == money.amount && getClass().equals(money.getClass());
      }
  ```

### 現在の ToDo リスト

- 現在の ToDo リストは以下の通り
  - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
  - [x] 5USD \* 2 = 10USD
  - [x] amount を private にする
  - [x] Dollar の副作用どうする？
  - [ ] Money の丸め処理どうする？
  - [x] equals() メソッドを実装する
  - [ ] hashCode() メソッドを実装する
  - [ ] null との等価性比較
  - [ ] 他クラスとのオブジェクトとの等価性比較
  - [x] 5CHF \* 2 = 10CHF
  - [ ] Dollar と Franc の重複を除去する
  - [x] equals() の一般化
  - [ ] times() の一般化
  - [x] Franc と Dollar を比較する

# 第 8 章 実装を隠す

## どうしたら、5USD + 10CHF = 10USD に近づけるのか？

### 今回の論点

- サブクラスである Dollar と Franc を Money に統合したい。
  - 現状、Dollar と Franc の実装はほとんど同じであり、冗長なコードを削減できる。
  - Dollar と Franc が別のクラスとして存在していると、5USD + 10CHF = 10USD のような通貨をまたぐ計算を実装する際に、複雑な条件分岐を伴うロジックが必要になってしまう。
  - 今後、新しい通貨 (Yen, Euro, Pound など) を追加するたびに 新しいクラスを作成しなければならず、拡張性が低くなってしまう。
- Dollar と Franc の Money への統合の足掛かりとして、完全な統合は諦めつつも、Dollar と Franc を Money に統一できるところから始める。
- 具体的には以下の通りである

  - Dollar と Franc の times() メソッド

    - 現状、以下の通り、戻り値は Dollar であるが、名目上の戻り値だけでも Money にすることはできるのではないか？

    ```java
      Dollar times(int multiplier) {
          return new Dollar(amount * multiplier);
      }
    ```

  - Money に Dollar と Franc のファクトリメソッドを持たせる
    - そうすることで、Dollar と Franc の外部からの呼び出しを減らすことができる。

### times() メソッドを Money に変更する

- 以下のように、times() メソッドを Money に変更する
- これにより、Dollar と Franc の times() メソッドを Money に統一することができる

  ```java
    Money times(int multiplier) {
        return new Dollar(amount * multiplier);
    }
  ```

### Dollar と Franc のファクトリメソッドを Money に追加する

1. テストコードの new Dollar と new Franc を money.dollar() と money.franc() に変更する

   ```java
   package money;

   import org.junit.jupiter.api.Test;
   import static org.junit.jupiter.api.Assertions.*;

   public class MoneyTest {
       @Test
       public void testMultiplication() {
           Money five = Money.dollar(5);
           assertEquals(Money.dollar(10), five.times(2));
           assertEquals(Money.dollar(15), five.times(3));
       }

       @Test
       public void testEquality() {
           assertTrue(Money.dollar(5).equals(Money.dollar(5)));
           assertFalse(Money.dollar(5).equals(Money.dollar(6)));
           assertTrue(Money.franc(5).equals(Money.franc(5)));
           assertFalse(Money.franc(5).equals(Money.franc(6)));
           assertFalse(Money.dollar(5).equals(Money.franc(5)));
       }

       @Test
       public void testFrancMultiplication() {
           Money five = Money.franc(5);
           assertEquals(Money.franc(10), five.times(2));
           assertEquals(Money.franc(15), five.times(3));
       }
   }
   ```

2. Dollar と Franc のファクトリメソッドを Money に追加する

   ```java
    package money;

    public abstract class Money {
        protected int amount;

        abstract Money times(int multiplier);

        public boolean equals(Object object) {
            Money money = (Money) object;
            return amount == money.amount && getClass().equals(money.getClass());
        }

        static Money dollar(int amount) {
            return new Dollar(amount);
        }

        static Money franc(int amount) {
            return new Franc(amount);
        }
    }
   ```

### 現在の ToDo リスト

- 現在の ToDo リストは以下の通り
  - 今回の改変により、「Dollar と Franc の重複を除去する」にかなり近づいた。
    - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
    - [x] 5USD \* 2 = 10USD
    - [x] amount を private にする
    - [x] Dollar の副作用どうする？
    - [ ] Money の丸め処理どうする？
    - [x] equals() メソッドを実装する
    - [ ] hashCode() メソッドを実装する
    - [ ] null との等価性比較
    - [ ] 他クラスとのオブジェクトとの等価性比較
    - [x] 5CHF \* 2 = 10CHF
    - [ ] Dollar と Franc の重複を除去する
    - [x] equals() の一般化
    - [ ] times() の一般化
    - [x] Franc と Dollar を比較する
    - [ ] 通貨の概念
    - [ ] testFrancMultiplication() を削除する？

### コラム

この章がやろうとしていることは一体なんなのか、それを理解するのに少し苦労した。要因は以下の通りだ。

1.  なぜ、Dollar と Franc を Money に統合するのか？現実世界では、Dollar と Franc は確かに別の概念であり、かつ Money の一種なのだから、Money を親クラスに持つ別々のサブクラスということでいいのでは？
2.  なぜ、times()の実際の戻り値が Dollar であるにもかかわらず、名目上の戻り値を Money にしたのか？

これらのの疑問については、通読するだけでは理解できなかったので ChatGPT と議論することにした。そして、自分なりには次のような理由があるからだと理解した。

> 1\. なぜ、Dollar と Franc を Money に統合するのか？現実世界では、Dollar と Franc は確かに別の概念であり、かつ Money の一種なのだから、Money を親クラスに持つ別々のサブクラスということでいいのでは？

- オブジェクト指向の観点で考えるならば、それが正解である。
- しかしながら、以下の３点の理由から、それでも Dollar と Franc を Money に統合することにメリットがあるといえる
  1.  Dollar と Franc は、その実装がほとんど同じであり、冗長である。
  2.  Dollar と Franc を別クラスとしたまま、5USD + 10CHF = 10USD のような通貨をまたぐ計算を行うには、複雑な条件分岐などが必要になってくる。
  3.  今後、新しい通貨 (Yen, Euro, Pound など) を追加するたびに 新しいクラスを作成しなければならないのは、非効率的である。
- よって、Dollar と Franc を Money に統合することにメリットがあるといえる。

> 2\. なぜ、times()の実際の戻り値が Dollar であるにもかかわらず、名目上の戻り値を Money にしたのか？

- 自分は、「Money が Dollar と Franc を表現する能力を有してから、times()の実際の戻り値と名目上の戻り値を共に Money にすればいいのではないか？」と考えた。
- しかし、現状、Money に Dollar と Franc を表現する能力はないが、せめて times()の名目上の戻り値だけでも Money にすることで、「叶えたい理想を一旦は諦め、妥協しつつも、可能な部分だけでも、少しずつ理想に近づけることで、次に取り組むべき課題がハッキリと見えてくる。」というテスト駆動開発の本質を表しているとわかった。

今回の章は自分にとって、難解だっただけに、学びが深かった。何より、2 の理解から、テスト駆動開発のセオリーである「スモールステップ」の本質を、僅かではあるが知識ではなく、体感で理解することができたと思う。今回のような、「理想を一気に叶えることはできないが、「叶えたい理想を一旦は諦め、妥協しつつも、可能な部分だけでも、少しずつ理想に近づけることで、次に取り組むべき課題がハッキリと見えてくる。」というスタンスは、精神的な負担や思考的の負担も少ないように思える。

# 第 9 章 歩幅の調整

## 「動作」を変更してから、「構造」を変更する

### 本題に入る前に

- Dollar と Franc の times() は当初より、戻り値が自身のコンストラクタによるものになっている

  ```java
      Money times(int multiplier) {
          return new Dollar(amount * multiplier);
      }
  ```

- 前回の改変により、Money には Dollar と Franc のファクトリメソッドが追加されているので、これを戻すことにする。

  ```java
      static Money dollar(int amount) {
          return new Dollar(amount);
      }

  ```

### 今回の論点

- どうしたら、Dollar や Franc というクラスに頼らずに、通貨（currency）の概念を表現することができるだろうか？
- Money が currency の概念を有するようにすればいい

### 「動作の変更」よりも「構造の変更」の方がリスクが高い

- これはこの章の内容を理解するために ChatGPT から聞いたことだが、「動作の変更」よりも「構造の変更」の方がリスクが多いらしい。
- つまり、どういうことかというと、例えば、Money に currency の概念を持たせるにあたって、
  - 「動作（メソッド）」を変更する方が簡単
  - 「構造（フィールド）」を変更する方が難しい　ということだ。
  - なぜなら、フィールドを追加は、コンストラクタや equal() など、他にも影響を及ぼすためである。
- なので、テスト駆動開発のセオリーである「スモールステップ」を遵守する意味でも、まずは currency の概念をメソッドで表現してから、フィールドへと移管していく。

### 手順

1. テストコードを追加する

   - 以下のように、テストコードを追加する

     ```java
     public void testCurrency() {
        assertEquals("USD", Money.dollar(1).currency());
        assertEquals("CHF", Money.franc(1).currency());
     }
     ```

2. currency メソッドを追加する

   - Dollar と Franc に currency メソッドを追加する

     ```java
     String currency() {
        return "USD";
     }
     ```

   - これで、動作として currency を表現することはできるようになった。
   - 次に、これを構造に変化させていく。

3. Dollar と Franc のフィールドで currency を表現するようにする

   ```java
   package money;

   public class Dollar extends Money {
       private String currency;

       Dollar(int amount) {
           this.amount = amount;
           this.currency = "USD";
       }

       String currency() {
           return currency;
       }

       Money times(int multiplier) {
           return Money.dollar(amount * multiplier);
       }
   }
   ```

4. Dollar と Franc の currency と currency() は同一の記述となったので、Money に引き上げる

   ```java
    package money;

    public abstract class Money {
        protected int amount;
        protected String currency;

        abstract Money times(int multiplier);

        public boolean equals(Object object) {
            Money money = (Money) object;
            return amount == money.amount && getClass().equals(money.getClass());
        }

        String currency() {
            return currency;
        }

        static Money dollar(int amount) {
            return new Dollar(amount);
        }

        static Money franc(int amount) {
            return new Franc(amount);
        }
    }
   ```

5. Dollar と Franc のコンストラクタを共通の処理にする。

   ```java
    Dollar(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
   ```

6. Dollar と Franc のコンストラクタを Money に引き上げる

   ```java
    Money(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
   ```

7. Dollar と Franc でこれを利用する

   ```java
    Dollar(int amount, String currency) {
        super(amount, currency);
    }
   ```

8. Money のファクトリメソッドを適応させる

   ```java
    static Money dollar(int amount) {
        return new Dollar(amount, "USD");
    }

    static Money franc(int amount) {
        return new Franc(amount, "CHF");
    }
   ```

### 現状の確認

- 現状、Dollar と Franc の内容は限りなく近くなった
- そろそろ、Dollar と Franc を Money に統合することができそうだ

  ```java
  package money;

  public class Dollar extends Money {

      Dollar(int amount, String currency) {
          super(amount, currency);
      }

      Money times(int multiplier) {
          return Money.dollar(amount * multiplier);
      }

  }
  ```

  ```java
      package money;

      public class Franc extends Money {

          Franc(int amount, String currency) {
              super(amount, currency);
          }

          Money times(int multiplier) {
              return Money.franc(amount * multiplier);
          }
      }
  ```

- 現在の ToDo リスト
  - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
  - [x] 5USD \* 2 = 10USD
  - [x] amount を private にする
  - [x] Dollar の副作用どうする？
  - [ ] Money の丸め処理どうする？
  - [x] equals() メソッドを実装する
  - [ ] hashCode() メソッドを実装する
  - [ ] null との等価性比較
  - [ ] 他クラスとのオブジェクトとの等価性比較
  - [x] 5CHF \* 2 = 10CHF
  - [ ] Dollar と Franc の重複を除去する
  - [x] equals() の一般化
  - [ ] times() の一般化
  - [x] Franc と Dollar を比較する
  - [x] 通貨の概念
  - [ ] testFrancMultiplication() を削除する？

# 第 10 章 テストに聞いてみる

## 勇気ある撤退も必要。スモールステップを刻み直す。

### 今回の論点

- Money.dollar() で生み出された Money dollar オブジェクトや、Money.franc() で生み出された Money franc オブジェクト はコンストラクタの処理によりインスタンス変数に currency = "USD" や currency = "CHF" を持っている。
- Money dollar や Money franc は times() を行うときに、この currency を使用することができる。
- ならば、この currency を使用することで、times()を一般化することができるのではないか？

### 手順

1. Dollar の times() を改変してみる

   1. return new Dollar(amount \* multiplier); としてみる
      ```java
      Money times(int multiplier) {
          return Money.dollar(amount * multiplier);
      }
      ```
      を
      ```java
      Money times(int multiplier) {
          return new Money(amount * multiplier, currency);
      }
      ```
      と改変したいところだが、これは飛躍しすぎであるため、まずは、
      ```java
      Money times(int multiplier) {
          return new Dollar(amount * multiplier);
      }
      ```
      としてみる。
      - テストは通る。
   2. return new Money(amount \* multiplier, currency); としてみる
      - これがうまくいくのかどうか、少し不安である。
      - どこまで影響がでるのか、よく考えなければわからない。
      - しかし、テスト駆動開発では、１０分よく考えるより、どうなるのかをテストに聞いてみた方がいい。
      - コードを改変してみる。
        ```java
        Money times(int multiplier) {
            return new Money(amount * multiplier, currency);
        }
        ```
      - テストが通らない。テストの結果を見てみる。
        ```java
        testMultiplication()
        org.opentest4j.AssertionFailedError: expected: <money.Dollar@3549bca9> but was: <money.Money@4f25b795>
        ```
      - どうやら、assertEquals(Money.dollar(10), five.times(2)); の部分で、Dollar と Money が同じ型ではないために起きているようだ。
      - これは、equale() の実装の問題だが、等価性比較において、もはや型が重要なのではなく、currency と amount が合っているかどうかが重要なのだ。
   3. 一時撤退し、equals() の改変に取り組む

      1. こういうときは、一時撤退する。return new Dollar(amount \* multiplier); に戻す。
         ```java
         Money times(int multiplier) {
            return new Dollar(amount * multiplier, currency);
         }
         ```
      2. equals() についてのテストケースを追加する

         ```java
         public void testDifferentClassEquality() {
            assertTrue(new Money(10, "USD").equals(new Dollar(10, "USD")));
         }
         ```

      3. equals() を改変する

         - 以下のように equals() を改変し、テストを実行してみる
         - テストが通った！違うクラスであっても。currency と amount が同じなら等価だと判断されるようになったということだ！

         ```java
         public boolean equals(Object object) {
             Money money = (Money) object;
             return amount == money.amount && currency().equals(money.currency());
         }
         ```

   4. 満を持して、再度、return new Money(amount \* multiplier, currency); としてみる
      - 再度、times() を改変してみる。
        ```java
        Money times(int multiplier) {
            return new Money(amount * multiplier, currency);
        }
        ```
      - そして、テストを実行してみる。
      - テストが通った！等価性比較の方法を更新したことによって、テストの結果も望ましいものになった！
   5. Franc times() にも変更を適用する
   6. Dollar と Franc の times() が一致したので、Money に引き上げる。

      - それでも、テストは通る。テストが通ることの安心感がハンパない！

      ```java
        package money;

        public class Money {
        protected int amount;
        protected String currency;

            Money(int amount, String currency) {
                this.amount = amount;
                this.currency = currency;
            }

            Money times(int multiplier) {
                return new Money(amount * multiplier, currency);
            }

            public boolean equals(Object object) {
                Money money = (Money) object;
                return amount == money.amount && currency().equals(money.currency());
            }

            String currency() {
                return currency;
            }

            static Money dollar(int amount) {
                return new Dollar(amount, "USD");
            }

            static Money franc(int amount) {
                return new Franc(amount, "CHF");
            }

        }
      ```

### 現在の ToDo リスト

- [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
- [x] 5USD \* 2 = 10USD
- [x] amount を private にする
- [x] Dollar の副作用どうする？
- [ ] Money の丸め処理どうする？
- [x] equals() メソッドを実装する
- [ ] hashCode() メソッドを実装する
- [ ] null との等価性比較
- [ ] 他クラスとのオブジェクトとの等価性比較
- [x] 5CHF \* 2 = 10CHF
- [ ] Dollar と Franc の重複を除去する
- [x] equals() の一般化
- [x] times() の一般化
- [x] Franc と Dollar を比較する
- [x] 通貨の概念
- [ ] testFrancMultiplication() を削除する？

# 第 11 章 不要になったら消す

## テストで安全を確認しながら、参照を減らしていく

### 今回の目的

- いよいよ、Dollar と Franc を消すときがきた
- プロジェクトの中から、Dollar と Franc を参照していた部分を Money に変更し、
- テストで安全性を確認しながら、Dollar と Franc を消していく

### 手順

1. Money に存在する Dollar と Franc への参照を Money に変更する

   ```java
       static Money dollar(int amount) {
           return new Money(amount, "USD");
           //return new Dollar(amount, "USD");
       }

       static Money franc(int amount) {
           return new Money(amount, "CHF");
           //return new Franc(amount, "CHF");
       }
   ```

2. テストコードを変更する

   - テストコードの中に存在する Dollar と Franc やいらなくなったテストを削除する

3. Dollar と Franc を削除する

### 現在の 状況

- 現在のコード

  ```java
  package money;

  import org.junit.jupiter.api.Test;
  import static org.junit.jupiter.api.Assertions.*;

  public class MoneyTest {
      @Test
      public void testMultiplication() {
          Money five = Money.dollar(5);
          assertEquals(Money.dollar(10), five.times(2));
          assertEquals(Money.dollar(15), five.times(3));
      }

      @Test
      public void testFrancMultiplication() {
          Money five = Money.franc(5);
          assertEquals(Money.franc(10), five.times(2));
          assertEquals(Money.franc(15), five.times(3));
      }

      @Test
      public void testEquality() {
          assertTrue(Money.dollar(5).equals(Money.dollar(5)));
          assertFalse(Money.dollar(5).equals(Money.dollar(6)));
          assertTrue(Money.franc(5).equals(Money.franc(5)));
          assertFalse(Money.franc(5).equals(Money.franc(6)));
          assertFalse(Money.dollar(5).equals(Money.franc(5)));
      }

      @Test
      public void testCurrency() {
          assertEquals("USD", Money.dollar(1).currency());
          assertEquals("CHF", Money.franc(1).currency());
      }

  }
  ```

  ```java
  package money;

  public class Money {
      protected int amount;
      protected String currency;

      Money(int amount, String currency) {
          this.amount = amount;
          this.currency = currency;
      }

      Money times(int multiplier) {
          return new Money(amount * multiplier, currency);
      }

      public boolean equals(Object object) {
          Money money = (Money) object;
          return amount == money.amount && currency().equals(money.currency());
      }

      String currency() {
          return currency;
      }

      static Money dollar(int amount) {
          return new Money(amount, "USD");
      }

      static Money franc(int amount) {
          return new Money(amount, "CHF");
      }
  }
  ```

- 現在の ToDo リスト
  - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
  - [x] 5USD \* 2 = 10USD
  - [x] amount を private にする
  - [x] Dollar の副作用どうする？
  - [ ] Money の丸め処理どうする？
  - [x] equals() メソッドを実装する
  - [ ] hashCode() メソッドを実装する
  - [ ] null との等価性比較
  - [ ] 他クラスとのオブジェクトとの等価性比較
  - [x] 5CHF \* 2 = 10CHF
  - [x] Dollar と Franc の重複を除去する
  - [x] equals() の一般化
  - [x] times() の一般化
  - [x] Franc と Dollar を比較する
  - [x] 通貨の概念
  - [x] testFrancMultiplication() を削除する？
