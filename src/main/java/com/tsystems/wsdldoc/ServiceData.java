package com.tsystems.wsdldoc;

import java.util.List;

/**
 * By: Alexey Matveev
 * Date: 25.08.2016
 * Time: 17:20
 */
public record ServiceData(String name, List<MethodData> methods) {}
