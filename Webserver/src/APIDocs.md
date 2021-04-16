The API can be reached under /api. All calls need to be sent to this url. 

**GetChildren**
----
  Returns the children of a parent.

* **Method:**

  `GET`
  
*  **URL Params**

   **Required:**
 
   `parent=[string]`
   
    **Optional:**
    `limit=[int]`

* **Success Response:**

    **Content:** `{ ["United States","Germany","France","Australia","China","India"] }`
 
* **Error Response:**

	No children found:
    **Content:** `{ ["null"] }`

* **Sample Call:**

  ```js
    $.ajax({
      url: "./api",
      data: {
	      parent: "country"
		},
      dataType: "json",
      type : "GET",
      success : function(r) {
        console.log(r);
      }
    });
  ```
**GetParent**
----
  Returns the parent of a child.

* **Method:**

  `GET`
  
*  **URL Params**

   **Required:**
 
   `child=[string]`

* **Success Response:**

    **Content:** `{genre}`
 
* **Error Response:**

	No parent found:
    **Content:** `{ null }`

* **Sample Call:**

  ```js
    $.ajax({
      url: "./api",
      data: {
	      child: "country"
		},
      type : "GET",
      success : function(r) {
        console.log(r);
      }
    });
  ```
  Note that the data type for this call is not JSON. The reason is that only a string is returned, which is not interpreted as JSON by all browsers. It is possible that a request that explicitly states JSON as a datatype returns a status 200 but with a message saying "parseerror". In that case you can still find the parent in the responseText field. 

**GetTaxonomyTree**
----
  Returns a JSON object representing a taxonomy tree.

* **Method:**

  `GET`
  
*  **URL Params**

   **Required:**
 
   `root=[string]`

* **Success Response:**

    **Content:** `
    [{"parent":"null",
      "children":[
         {
            "parent":"economist",
            "children":[
               {
                  "parent":"american economist",
                  "children":[
                  ],
                  "name":"Brad DeLong"
               },
               {
                  "parent":"american economist",
                  "children":[
                  ],
                  "name":"Walt Rostow"
               },
               {
                  "parent":"american economist",
                  "children":[
                  ],
                  "name":"Med Yones"
               },
               {
                  "parent":"american economist",
                  "name":"Wei Xiong"
               },
               {
                  "parent":"american economist",
                  "name":"Benn Steil"
               },
               {
                  "parent":"american economist",
                  "name":"Gary Stern"
               }
            ],
            "name":"american economist"
         },
         {
            "parent":"economist",
            "children":[
               {
                  "parent":"classical economist",
                  "children":[
                  ],
                  "name":"Adam Smith"
               },
               {
                  "parent":"classical economist",
                  "children":[
                  ],
                  "name":"David Ricardo"
               },
               {
                  "parent":"classical economist",
                  "name":"Karl Marx"
               },
               {
                  "parent":"classical economist",
                  "name":"Ricardo"
               },
               {
                  "parent":"classical economist",
                  "name":"Malthus"
               },
               {
                  "parent":"classical economist",
                  "name":"Thomas Malthus"
               }
            ],
            "name":"classical economist"
         },
         {
            "parent":"economist",
            "children":[
            ],
            "name":"Milton Friedman"
         },
         {
            "parent":"economist",
            "children":[
            ],
            "name":"Paul Krugman"
         },
         {
            "parent":"economist",
            "name":"british economist"
         },
         {
            "parent":"economist",
            "name":"indian economist"
         }
      ],
      "name":"economist"}]
      `

* **Error Response:**

	No children found:
    **Content:** `{ [{"parent":"null","children":[],"name":"yourRequestParam"}] }`

* **Sample Call:**

  ```js
    $.ajax({
      url: "./api",
      data: {
	      root: "economist"
		},
      dataType: "json",
      type : "GET",
      success : function(r) {
        console.log(r);
      }
    });
  ```
