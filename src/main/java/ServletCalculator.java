import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

public class ServletCalculator extends HttpServlet {
    public static final String CALC_COOKIE_NAME = "calc_id";
    private CalculatorManager manager;

    public ServletCalculator(CalculatorManager manager) {
        this.manager = manager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Calculator calc;
        Cookie[] cookies = req.getCookies();
        if (cookies == null || !hasCalcCookie(cookies)) {
            // create calculator
            calc = manager.getOrCreate();
            // create cookie
            resp.addCookie(new Cookie(CALC_COOKIE_NAME, String.valueOf(calc.getId())));
        } else {
            Cookie c = Stream.of(cookies).filter(cookie -> cookie.getName().equals(CALC_COOKIE_NAME)).findAny().get();
            // parse cookie
            int id = Integer.parseInt(c.getValue());
            // get the calculator
            calc = manager.getOrCreate(id);
        }

        Map<String, String[]> m = req.getParameterMap();
        StringBuilder content = new StringBuilder();
        if (!m.isEmpty()) {
            calc.setData(m.get("x")[0],m.get("y")[0]);
            content.append("Data successfully set");
        } else {
            try {
                content.append(String.format("calculated: %d\n", calc.add()));
            } catch (IllegalArgumentException e) {
                content.append(e.getMessage());
            }
        }
        resp.getWriter().write(content.toString());
    }

    private boolean hasCalcCookie(Cookie[] cookies) {
        return Stream.of(cookies).parallel().anyMatch(cookie -> cookie.getName().equals(CALC_COOKIE_NAME));
    }
}
