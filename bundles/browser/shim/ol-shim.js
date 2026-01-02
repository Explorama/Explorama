import * as ol from 'ol';
import * as olColor from 'ol/color.js';
import * as olControl from 'ol/control.js';
import * as olCoordinate from 'ol/coordinate.js'; // Import coordinate
import * as olEasing from 'ol/easing.js';
import * as olEvents from 'ol/events.js';
import * as olExtent from 'ol/extent.js';
import * as olFormat from 'ol/format.js';
import * as olGeom from 'ol/geom.js';
import * as olHas from 'ol/has.js';
import * as olInteraction from 'ol/interaction.js';
import * as olLayer from 'ol/layer.js';
import BaseLayer from 'ol/layer/Base.js';
import * as olLoadingstrategy from 'ol/loadingstrategy.js';
import * as olProj from 'ol/proj.js';
import * as olRender from 'ol/render.js';
import * as olSource from 'ol/source.js';
import * as olSphere from 'ol/sphere.js';
import * as olStyle from 'ol/style.js';
import * as olTilegrid from 'ol/tilegrid.js';
import * as olUtil from 'ol/util.js';
import View from 'ol/View.js';

// Create a mutable object extending the base ol namespace
const olGlobal = Object.assign({}, ol);

// Attach missing modules
olGlobal.color = olColor;
olGlobal.control = olControl;
olGlobal.coordinate = olCoordinate; // Attach coordinate
olGlobal.easing = olEasing;
olGlobal.events = olEvents;
olGlobal.extent = olExtent;
olGlobal.format = olFormat;
olGlobal.geom = olGeom;
olGlobal.has = olHas;
olGlobal.interaction = olInteraction;

// Handle ol.layer
const mutableLayer = Object.assign({}, olLayer);
mutableLayer.Base = BaseLayer;
olGlobal.layer = mutableLayer;

olGlobal.loadingstrategy = olLoadingstrategy;
olGlobal.proj = olProj;
olGlobal.render = olRender;
olGlobal.source = olSource;
olGlobal.sphere = olSphere;
olGlobal.Sphere = olSphere;
olGlobal.style = olStyle;
olGlobal.tilegrid = olTilegrid;
olGlobal.util = olUtil;

if (!olGlobal.View) {
    olGlobal.View = View;
}

if (typeof window !== 'undefined') {
    window.ol = olGlobal;
}

export default olGlobal;