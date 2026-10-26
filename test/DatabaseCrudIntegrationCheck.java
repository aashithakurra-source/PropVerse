import com.propverse.dao.RecordDAO;
import com.propverse.database.DBConnection;
import com.propverse.service.AuthenticationService;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/** Optional live MySQL integration check; all test rows are rolled back. */
public class DatabaseCrudIntegrationCheck {
    public static void main(String[] args) throws Exception {
        RecordDAO dao=new RecordDAO();
        try(Connection c=DBConnection.getConnection()) {
            if(!"propverse_db".equalsIgnoreCase(c.getCatalog()))
                throw new AssertionError("Expected propverse_db, connected to "+c.getCatalog());
            if(new AuthenticationService().login("__propverse_missing_user__", "non-secret-test-value".toCharArray()))
                throw new AssertionError("A missing username unexpectedly authenticated");
            c.setAutoCommit(false);
            try {
                String marker="PV_TEST_"+Long.toUnsignedString(System.nanoTime());
                String projectName=marker+" Project";
                dao.save(c,RecordDAO.PROJECT,null,List.of(projectName,"Integration test site","PropVerse test client","2026-10-01","2027-10-01","1000.00","ONGOING","15"));
                Map<String,Object> project=row(dao.all(c,RecordDAO.PROJECT,projectName,Map.of()));
                int projectId=number(project,"project_id");
                dao.save(c,RecordDAO.WORKER,null,List.of(marker+" Worker","Mason","+91 90000 00001",marker.toLowerCase()+"@example.com",String.valueOf(projectId),"AVAILABLE"));
                Map<String,Object> worker=row(dao.all(c,RecordDAO.WORKER,marker+" Worker",Map.of()));
                int workerId=number(worker,"worker_id");
                String taskName=marker+" Task";
                dao.save(c,RecordDAO.TASK,null,List.of(taskName,String.valueOf(projectId),String.valueOf(workerId),"2026-10-02","2026-10-20","IN_PROGRESS","15","DAO integration check"));
                Map<String,Object> task=row(dao.all(c,RecordDAO.TASK,taskName,Map.of()));
                int taskId=number(task,"task_id");
                String issueTitle=marker+" Issue";
                dao.save(c,RecordDAO.ISSUE,null,List.of(String.valueOf(projectId),issueTitle,"DAO integration check","MEDIUM","OPEN",""));
                Map<String,Object> issue=row(dao.all(c,RecordDAO.ISSUE,issueTitle,Map.of()));
                int issueId=number(issue,"issue_id");

                dao.save(c,RecordDAO.PROJECT,projectId,List.of(projectName,"Updated integration site","PropVerse test client","2026-10-01","2027-10-01","1000.00","ONGOING","20"));
                dao.save(c,RecordDAO.WORKER,workerId,List.of(marker+" Worker","Mason","+91 90000 00001",marker.toLowerCase()+"@example.com",String.valueOf(projectId),"BUSY"));
                dao.save(c,RecordDAO.TASK,taskId,List.of(taskName,String.valueOf(projectId),String.valueOf(workerId),"2026-10-02","2026-10-20","COMPLETED","100","Updated integration check"));
                dao.save(c,RecordDAO.ISSUE,issueId,List.of(String.valueOf(projectId),issueTitle,"DAO integration check","HIGH","IN_PROGRESS","Being reviewed"));

                if(dao.all(c,RecordDAO.PROJECT,"Updated integration site",Map.of()).isEmpty())throw new AssertionError("Project search/update failed");
                if(dao.all(c,RecordDAO.WORKER,"",Map.of("availability_status","BUSY")).stream().noneMatch(x->number(x,"worker_id")==workerId))throw new AssertionError("Worker filter/update failed");
                if(dao.all(c,RecordDAO.TASK,"",Map.of("project_id",String.valueOf(projectId),"status","COMPLETED")).stream().noneMatch(x->number(x,"task_id")==taskId))throw new AssertionError("Task filter/update failed");
                if(dao.all(c,RecordDAO.ISSUE,"",Map.of("project_id",String.valueOf(projectId),"priority","HIGH","status","IN_PROGRESS")).stream().noneMatch(x->number(x,"issue_id")==issueId))throw new AssertionError("Issue filter/update failed");

                dao.delete(c,RecordDAO.TASK,taskId);dao.delete(c,RecordDAO.ISSUE,issueId);dao.delete(c,RecordDAO.WORKER,workerId);dao.delete(c,RecordDAO.PROJECT,projectId);
                c.rollback();
                if(!dao.all(c,RecordDAO.PROJECT,marker,Map.of()).isEmpty()
                        ||!dao.all(c,RecordDAO.WORKER,marker,Map.of()).isEmpty()
                        ||!dao.all(c,RecordDAO.TASK,marker,Map.of()).isEmpty()
                        ||!dao.all(c,RecordDAO.ISSUE,marker,Map.of()).isEmpty())
                    throw new AssertionError("The test transaction left rows behind after rollback");
                System.out.println("Database CRUD integration checks passed and transaction rolled back.");
            } catch(Exception|Error ex) { c.rollback(); throw ex; }
        }
    }
    private static Map<String,Object> row(List<Map<String,Object>> rows){if(rows.isEmpty())throw new AssertionError("Expected inserted row was not found");return rows.get(0);}
    private static int number(Map<String,Object> row,String key){return ((Number)row.get(key)).intValue();}
}
