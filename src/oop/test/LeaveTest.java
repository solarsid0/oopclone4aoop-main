import oop.classes.actors.HR;
import oop.classes.actors.ImmediateSupervisor;
import oop.classes.empselfservice.LeaveRequest;
import oop.classes.enums.LeaveName;
import oop.classes.enums.ApprovalStatus;
import java.time.LocalDate;

public class LeaveTest {
    public static void main(String[] args) {
        // ✅ Create Supervisor & HR
        ImmediateSupervisor supervisor = new ImmediateSupervisor(20001, "John", "Doe", "john.doe@company.com", "password123", "Supervisor");
        HR hr = new HR(30001, "Jane", "Smith", "jane.smith@company.com", "password123", "HR");

        // ✅ Create Leave Request
        String empId = "10001";  
        LeaveRequest request = new LeaveRequest(empId, LeaveName.VACATION, LocalDate.of(2025, 3, 10), LocalDate.of(2025, 3, 12), "Vacation trip");

        System.out.println("\n🔹 Leave Request Submitted:");
        System.out.println("Employee ID: " + request.getEmployeeId());
        System.out.println("Leave Type: " + request.getLeaveType());
        System.out.println("Start Date: " + request.getStartDate());
        System.out.println("End Date: " + request.getEndDate());
        System.out.println("Status: " + request.getStatus());

        // ✅ Store the leave request in the supervisor’s records
        supervisor.addLeaveRequest(request);  // 🔥 FIX: Ensure Supervisor knows about the request

        // ✅ Supervisor Approves Leave
        boolean supervisorApproved = supervisor.approveLeaveRequest(request.getRequestId());
        if (supervisorApproved) {
            System.out.println("\n✅ Supervisor approved the leave request.");
        } else {
            System.out.println("\n❌ Supervisor rejected the leave request.");
        }

        // ✅ If Supervisor approves, send to HR
        if (supervisorApproved) {
            hr.addLeaveRequest(request);  // 🔥 FIX: Now HR knows about the request too
        }

        // ✅ HR Final Approval
        boolean hrApproved = hr.approveLeaveRequest(request.getRequestId());
        if (hrApproved) {
            request.setStatus(ApprovalStatus.APPROVED);
            System.out.println("\n✅ HR approved the leave request. Final Status: " + request.getStatus());
        } else {
            System.out.println("\n❌ HR rejected the leave request.");
        }
    }
}