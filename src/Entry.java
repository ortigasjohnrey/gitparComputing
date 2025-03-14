import java.sql.Date;

public class Entry {
    private int entryID, posted;
    private Date datePosted;
    private String docNumber, businessCode, locationCode, moduleCode, accountCode, normalBalance;
    private double amount, amount2, credit, debit, finalAmount;

    public Entry(int entryID, int posted, Date datePosted, String docNumber, String businessCode,
                 String locationCode, String moduleCode, String accountCode, String normalBalance,
                 double amount, double amount2, double credit, double debit, double finalAmount) {
        this.entryID = entryID;
        this.posted = posted;
        this.datePosted = datePosted;
        this.docNumber = docNumber;
        this.businessCode = businessCode;
        this.locationCode = locationCode;
        this.moduleCode = moduleCode;
        this.accountCode = accountCode;
        this.normalBalance = normalBalance;
        this.amount = amount;
        this.amount2 = amount2;
        this.credit = credit;
        this.debit = debit;
        this.finalAmount = finalAmount;
    }

    public Object[] toObjectArray() {
        return new Object[]{
            entryID, posted, datePosted, docNumber, businessCode, locationCode,
            moduleCode, accountCode, normalBalance, amount, amount2, credit, debit, finalAmount
        };
    }
}
