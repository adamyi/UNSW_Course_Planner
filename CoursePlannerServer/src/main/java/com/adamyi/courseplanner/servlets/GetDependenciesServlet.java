package com.adamyi.courseplanner.servlets;

import com.adamyi.courseplanner.nlp.NLPTask;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
@WebServlet(name = "GetDependenciesServlet", value="/getDependencies")
public class GetDependenciesServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();

        out.println("Requesting dependencies for class" + request.getParameter("class"));

        NLPTask task = new NLPTask("(COMP1927 or COMP1928), or (COMP1538, and SENG1031, or MATH1081)");
        task.run();


    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
