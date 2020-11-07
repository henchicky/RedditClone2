package filter;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import managedbean.UserManagedBean;

public class AuthenticationFilter implements Filter {

    @Inject
    private UserManagedBean userManagedBean;

    public AuthenticationFilter() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request1 = (HttpServletRequest) request;

        if (userManagedBean == null || userManagedBean.getCurrentUser() == null) {
            //redirect to login page if user is not logged in and trying to access "authoriseduser/*" paths
            ((HttpServletResponse) response).sendRedirect(request1.getContextPath() + "/index.xhtml");
        } else {
            //authenticated - continue
            chain.doFilter(request1, response);
        }
    }

    public void destroy() {
        //do nothing
    }

    public void init(FilterConfig filterConfig) {
        //do nothing
    }
}
