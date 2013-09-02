package dk.lessismore.nojpa.masterworker.observer;

import dk.lessismore.nojpa.masterworker.messages.observer.UpdateMessage;
import dk.lessismore.nojpa.masterworker.messages.observer.ObserverJobMessage;
import dk.lessismore.nojpa.masterworker.messages.observer.ObserverWorkerMessage;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 28-01-2011
 * Time: 02:05:15
 * To change this template use File | Settings | File Templates.
 */
public class StatusObserverServlet  extends HttpServlet {


    protected static final StringBuilder builder = new StringBuilder();
    protected static ServletObserver servletObserver = new ServletObserver();



    static class ServletObserver extends AbstractObserver {
        @Override
        public void update(UpdateMessage updateMessage) {
            synchronized (builder) {
                builder.delete(0, builder.length());
                builder.append("<html>\n" +
                        "  <head>\n" +
                        "      <title>Status @ " + (new Date()) +"</title>\n" +
                        "      <meta http-equiv=\"refresh\" content=\"2\"> \n" +
                        "  </head>\n" +
                        "  <body>\n"
                        );
                builder.append("<table border=1>");
                builder.append("<tr><td colspan=\"9\" align=\"center\"><h2>JOB's</h2></td></tr>");
                builder.append("<tr><td>JobID</td><td>Status</td><td>ClassName</td><td>Progress</td><td>Date</td><td>SequenceNumber</td><td>Status</td><td>WorkerFailureCount</td><td>Worker</tr>");
                for (ObserverJobMessage job: updateMessage.getObserverJobMessages()) {
                    builder.append("<tr>");
                    builder.append("<td>");
                    builder.append(job.getJobID());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(job.getStatus());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(job.getExecutorClassName());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(job.getProgress());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(job.getDate().getTime());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(job.getSequenceNumber());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(job.getStatus());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(job.getWorkerFailureCount());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(job.getWorker());
                    builder.append("</td>");
                    builder.append("</tr>");
                }
                builder.append("</table>");
                builder.append("<table border=1>");
                builder.append("<tr><td colspan=\"6\" align=\"center\"><h2>WORKER's</h2></td></tr>");
                builder.append("<tr><td>Address</td><td>Idle</td><td>SystemLoad</td><td>VmMemoryUsage</td><td>Classes</td><td>Problem</td><tr>");
                for (ObserverWorkerMessage worker: updateMessage.getObserverWorkerMessages()) {
                    builder.append("<tr>");
                    builder.append("<td>");
                    builder.append(worker.getAddress());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(worker.getIdle());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(worker.getSystemLoad());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(worker.getVmMemoryUsage());
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(formatClassesNames(worker.getKnownClasses()));
                    builder.append("</td>");
                    builder.append("<td>");
                    builder.append(worker.getProblem());
                    builder.append("</td>");
                    builder.append("</tr>");
                }
                builder.append("</table>");
                builder.append("</body></html>");
                
            }
        }

        @Override
        protected void onConnectionError(IOException e) {
            synchronized (builder) {
                builder.delete(0, builder.length());
                builder.append("<html>\n" +
                        "  <head>\n" +
                        "      <title>Status @ " + (new Date()) +"</title>\n" +
                        "      <meta http-equiv=\"refresh\" content=\"2\"> \n" +
                        "  </head>\n" +
                        "  <body>\n");
                builder.append("CONNECTION ERROR: " + e.getMessage());
                builder.append("</body></html>");
            }
        }

        protected String formatClassesNames(Collection<String> classNames) {
            StringBuilder builder = new StringBuilder();
            for (String className: classNames) {
                String[] split = className.split("\\.");
                builder.append(split[split.length -1]);
                builder.append(", ");
            }
            int last = builder.length();
            if (last > 2) {
                builder.delete(last-2, last);
            }
            return builder.toString();
        }


    }



    public void doGet(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException {
        doService( request,  res);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException {
        doService( request,  res);
    }

    public void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try{
            PrintWriter out = response.getWriter();
            response.setContentType("text/html");
            synchronized (builder) {
                out.write(builder.toString());
            }
            out.flush();
            out.close();
            return;
	} catch(Exception e){
	    e.printStackTrace();
	}
    }


}
