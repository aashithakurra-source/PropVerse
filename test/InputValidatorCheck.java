import com.propverse.util.InputValidator;
import com.propverse.util.PasswordUtil;
public class InputValidatorCheck {
 public static void main(String[] args) {
  if(InputValidator.progress("100")!=100) throw new AssertionError();
  expectFailure(()->InputValidator.progress("101"));
  expectFailure(()->InputValidator.required(" ","Name"));
  expectFailure(()->InputValidator.date("25/10/2026"));
  InputValidator.orderedDates("2026-01-01","2026-01-02","Deadline");
  expectFailure(()->InputValidator.orderedDates("2026-01-02","2026-01-01","Deadline"));
  expectFailure(()->InputValidator.nonNegativeMoney("-1"));
  expectFailure(()->InputValidator.nonNegativeMoney("NaNish"));
  InputValidator.email("site.manager@example.org");
  expectFailure(()->InputValidator.email("not-an-email"));
  InputValidator.phone("+91 98765 43210");
  expectFailure(()->InputValidator.phone("phone"));
  char[] secret="viva-demo-password".toCharArray();
  String hash=PasswordUtil.hash(secret);
  if(!PasswordUtil.verify(secret,hash)||PasswordUtil.verify("wrong-password".toCharArray(),hash)) throw new AssertionError("Password hash verification failed");
  System.out.println("InputValidator checks passed.");
 }
 private static void expectFailure(Runnable r){try{r.run();throw new AssertionError("Expected validation failure");}catch(IllegalArgumentException expected){}}
}
