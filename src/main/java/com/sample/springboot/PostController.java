package com.sample.springboot;

import com.sample.utils.LoggerPost;
import com.sample.utils.LoginInfo;
import com.sample.utils.PostDB;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PostController {

    @Autowired
    private Environment env;

    private static final String logAttr = "LOGGERPOST";

    LoggerPost getLogger(HttpSession sesh) {
        String logPath = env.getProperty("post.log.file");
        String known_hosts_path = env.getProperty("known_hosts_path");
        LoggerPost pl = (LoggerPost)sesh.getAttribute(logAttr);
        if (pl == null) {
            pl = new LoggerPost(logPath, known_hosts_path);
            sesh.setAttribute(logAttr, pl);
        }
        return pl;
    }

    @RequestMapping(value = "/")
    public String Index(Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        return "index";
    }

    @RequestMapping(value = "/login", method=RequestMethod.GET)
    public String Login(Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        return "login";
    }

    @PostMapping(value = "/login")
    public String Login(Model model, HttpSession sesh, HttpServletRequest req) {
        LoggerPost pl = getLogger(sesh);
        LoginInfo login = new LoginInfo(
            req.getParameter("dbHost"),
            req.getParameter("dbDBName"),
            req.getParameter("dbUser"),
            req.getParameter("dbPass"),
            req.getParameter("jumpHost"),
            req.getParameter("jumpUser"),
            req.getParameter("jumpPass"));
        if (login.jumpHost == "") {
            login.jumpHost = null; login.jumpUser = null; login.jumpPass = null;
        }
        pl.Login(login, PostDB.MARIADB_DRIVER);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        if (!pl.GetLoggedIn())
            model.addAttribute("loginFailed", true);
        else
            return "index";

        return "login";
    }

    @RequestMapping(value = "/login_local")
    public String LoginLocal(Model model, HttpSession sesh, HttpServletRequest req) {
        LoggerPost pl = getLogger(sesh);
        LoginInfo login = new LoginInfo("", "", "", "", null, null, null);
        pl.Login(login, PostDB.H2_DRIVER);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        return "index";
    }

    @RequestMapping(value = "/logout")
    public String Logout(Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        pl.Logout();
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        return "index";
    }

    @GetMapping(value = "/api")
    public String AddPostPage(Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        return "index";
    }

    @PostMapping(value = "/api")
    public String AddPost(@RequestParam("post_text") String post,
    Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        if (pl.GetLoggedIn())
            pl.PostToDB(post);
        return "index";
    }

    @RequestMapping(value = "/history")
    public String GetAllPosts(Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        model.addAttribute("posts", pl.GetPostsListFromDB());
        return "history";
    }

    @GetMapping(value = "/delete")
    public String DeletePostPage(Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        return "delete";
    }

    @PostMapping(value = "/delete")
    public String DeletePost(@RequestParam("post_text") String deleteText,
    Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        if (!deleteText.isEmpty()) {
            boolean deleted = pl.DeletePostFromDB(deleteText);
            model.addAttribute("deleted", deleted);
            model.addAttribute("deleteAttempted", true);
        }
        return "delete";
    }

}

