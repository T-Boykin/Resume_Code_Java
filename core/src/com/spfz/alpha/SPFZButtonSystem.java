package com.spfz.alpha;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.NodeComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.components.ViewPortComponent;
import com.uwsoft.editor.renderer.components.ZIndexComponent;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;

public class SPFZButtonSystem extends IteratingSystem
{

	spfzTrial trial;
	
	public SPFZButtonSystem(Family family, spfzTrial trial)
	{
		super(family);
		this.trial = trial;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime)
	{
    NodeComponent nodeComponent = ComponentRetriever.get(entity, NodeComponent.class);

    if(nodeComponent == null) return;

    for (int i = 0; i < nodeComponent.children.size; i++) {
        Entity childEntity = nodeComponent.children.get(i);
        MainItemComponent childMainItemComponent = ComponentRetriever.get(childEntity, MainItemComponent.class);
        childMainItemComponent.visible = true;
    }

    ViewPortComponent camera = ComponentRetriever.get(entity, ViewPortComponent.class);
    if(camera != null) {
        // if camera is on this entity, then it should not be processed
        return;
    }


    for (int i = 0; i < nodeComponent.children.size; i++) {
        Entity childEntity = nodeComponent.children.get(i);
        MainItemComponent childMainItemComponent = ComponentRetriever.get(childEntity, MainItemComponent.class);
        ZIndexComponent childZComponent = ComponentRetriever.get(childEntity, ZIndexComponent.class);

    }

}

	 // Method returns a boolean stating that the user has dragged off of the
		// button so we do not process the button
	public boolean draggedfrmbtn(String button, boolean haschildren, String parent) 
		{
			Vector3 vec3 = new Vector3();
			Vector3 transpar = new Vector3(0, 0, 0);
			Vector2 dimwh = new Vector2();
			TransformComponent transcomponent;
			DimensionsComponent dimcomponent;

			if (haschildren)
			{
				if (trial.root.getChild(parent).getEntity() != null)
				{

					transcomponent = ComponentRetriever.get(trial.root.getChild(parent).getEntity(), TransformComponent.class);

					transpar.x = transcomponent.x;
					transpar.y = transcomponent.y;

					transcomponent = ComponentRetriever.get(trial.root.getChild(parent).getChild(button).getEntity(),
							TransformComponent.class);
					dimcomponent = ComponentRetriever.get(trial.root.getChild(parent).getChild(button).getEntity(),
							DimensionsComponent.class);
				}
				else
				{
					transcomponent = ComponentRetriever.get(trial.pauseroot.getChild(parent).getEntity(), TransformComponent.class);

					transpar.x = transcomponent.x;
					transpar.y = transcomponent.y;

					transcomponent = ComponentRetriever.get(trial.pauseroot.getChild(parent).getChild(button).getEntity(),
							TransformComponent.class);
					dimcomponent = ComponentRetriever.get(trial.pauseroot.getChild(parent).getChild(button).getEntity(),
							DimensionsComponent.class);
				}
			}
			else
			{
				transcomponent = ComponentRetriever.get(trial.root.getChild(button).getEntity(), TransformComponent.class);
				dimcomponent = ComponentRetriever.get(trial.root.getChild(button).getEntity(), DimensionsComponent.class);
			}

			if (trial.view == "portrait")
			{
				trial.viewportport.getCamera().update();
				vec3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				trial.viewportport.unproject(vec3);
			}
			else
			{
				trial.viewportland.getCamera().update();
				vec3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				trial.viewportland.unproject(vec3);
			}

			dimwh.x = dimcomponent.width * transcomponent.scaleX;
			dimwh.y = dimcomponent.height * transcomponent.scaleY;

			if (vec3.x >= transcomponent.x + transpar.x && vec3.x <= (transcomponent.x + dimwh.x + transpar.x)
					&& vec3.y >= transcomponent.y + transpar.y && vec3.y <= (transcomponent.y + dimwh.y + transpar.y))
			{
				return false;
			}
			else
			{
				return true;
			}

		}
		
}
