package money;

public class Sum implements Expression {
    Expression augend;
    Expression addend;

    Sum(Expression augend, Expression addend) {
        this.augend = augend;
        this.addend = addend;
    }

    public Money reduce(String to, Bank bank) {
        int amount = augend.reduce(to, bank).amount + addend.reduce(to, bank).amount;
        return new Money(amount, to);
    }

    public Expression plus(Expression addend) {
        return null;
    }
}
