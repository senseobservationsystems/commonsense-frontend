package nl.sense_os.commonsense.client.data;

import com.google.gwt.core.client.JavaScriptObject;

public class Cache {

    /**
     * Converts a String to a JavaScriptObject. Also converts any "embedded"
     * JSON-disguised-as-String objects, e.g. <code>{"foo":"{\"bar\":\"baz\"}"}</code> will work.
     * 
     * @param raw
     *            the raw String to convert to JSON
     * @return a JavaScriptObject containing the evaluated String
     */
    public final static native JavaScriptObject toJso(String raw) /*-{

		function stripslashes(str) {
			return (str + '').replace(/\\(.?)/g, function(s, n1) {
				switch (n1) {
				case '\\':
					return '\\';
				case '0':
					return '\u0000';
				case '':
					return '';
				default:
					return n1;
				}
			});
		}
		var stripped = stripslashes(raw);
		var jsonFixed = stripped.replace(/:\"{/g, ':{').replace(/}\"/g, '}');
		return eval('(' + jsonFixed + ')');
    }-*/;

    public final static native JavaScriptObject parseData(String sensor, JavaScriptObject jso,
            JavaScriptObject cache) /*-{

		function appendValue(label, newValue) {
			// find earlier data (add if needed)
			var index = cache.mapping[label];
			if (index == undefined) {
				// create new entry
				var newEntry = {
					'label' : label,
					'type' : typeof (newValue.value),
					'data' : [ newValue ]
				};

				// add new index
				index = cache.content.push(newEntry) - 1;
				cache.mapping[label] = index;

			} else {
				// push onto earlier entry
				cache.content[index].data.push(newValue);
			}
		}

		if (cache == undefined) {
			console.log("create new cache");
			cache = {
				'mapping' : {},
				'content' : []
			};
		}
		var data = jso.data;
		for ( var i = 0, len = data.length; i < len; i++) {
			var obj = data[i];
			var date = obj.date;
			var value = obj.value;

			if (!isNaN(value)) {
				// The value contains a number

				// prepare new value for the 'values' array
				var newValue = {
					'date' : parseFloat(date),
					'value' : parseFloat(value)
				};

				appendValue(sensor, newValue);

			} else if (typeof (value) == 'string') {
				// The value contains a string

				// prepare new value for the 'values' array
				var newValue = {
					'date' : parseFloat(date),
					'value' : value
				};

				appendValue(sensor, newValue);

			} else {
				// the value can contain multiple properties

				for (prop in value) {
					// prepare new value for the 'values' array
					var newValue = {
						'date' : parseFloat(date),
						'value' : value[prop]
					};

					appendValue(sensor + ' ' + prop, newValue);
				}
			}
		}
		console.log('converted ' + sensor);
		console.log(cache);
		return cache;
    }-*/;
}
