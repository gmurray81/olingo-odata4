<!--

Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.

-->
<html>
<body>
<h2>Hello World!</h2>
<a href="DemoService.svc/">OData Olingo V4 Demo Service - Expand and Select</a>
<h3>Sample Links</h3>
<ul>
    <li>
        <a href="DemoService.svc/Products(1)/?$expand=Category">Expand - /Products(1)/?$expand=Category</a>
    </li>
    <li>
        <a href="DemoService.svc/Products/?$expand=Category">Expand - /Products/?$expand=Category</a>
    </li>
    <li>
        <a href="DemoService.svc/Categories(1)/?$expand=RelProducts">Expand - /Categories(1)/?$expand=RelProducts</a>
    </li>
    <li>
        <a href="DemoService.svc/Categories/?$expand=RelProducts">Expand - /Categories/?$expand=RelProducts</a>
    </li>
</ul>


</body>
</html>
